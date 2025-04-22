# consumer.py
# RabbitMQ Consumer for SNS Data Ingestion

import pika
import os
import time
import json
import logging
import sys
from collections import deque # Using deque for potentially slightly more efficient appends

# --- Database Imports ---
import pymongo
import mysql.connector # Official MySQL Driver
from sqlalchemy import create_engine # Using SQLAlchemy for easier Pandas -> MySQL insertion
import pandas as pd

# --- Setup Logging ---
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(levelname)s - %(message)s',
                    stream=sys.stdout) # Log to standard output

# --- 1. Configuration ---
logging.info("Loading configuration from environment variables...")

# RabbitMQ Configuration
AMQP_HOST = os.getenv("RABBITMQ_HOST")
AMQP_PORT = int(os.getenv("RABBITMQ_PORT"))
AMQP_USER = os.getenv("RABBITMQ_USER")
AMQP_PWD = os.getenv("RABBITMQ_PASSWORD")
AMQP_VHOST = os.getenv("AMQP_VHOST", "/")

# Queues to listen to (Producers must send to these)
TRANSACTIONS_QUEUE = os.getenv("TRANSACTIONS_QUEUE", "q.transactions")
AUTH_LOGS_QUEUE = os.getenv("AUTH_LOGS_QUEUE", "q.auth_logs")

# Database Configuration
MYSQL_USER = os.getenv("MYSQL_USER")
MYSQL_PWD = os.getenv("MYSQL_PWD")
MYSQL_HOST = os.getenv("MYSQL_HOST") # Use the service name from docker-compose (e.g., 'db-activities')
MYSQL_DB_ACTIVITIES = os.getenv("MYSQL_DB_ACTIVITIES")
MYSQL_PORT = os.getenv("MYSQL_PORT")
MYSQL_TRANSACTIONS_TABLE = os.getenv("MYSQL_TRANSACTIONS_TABLE", "transactions")
# Add service_usage table if needed
# MYSQL_SERVICE_USAGE_TABLE = os.getenv("MYSQL_SERVICE_USAGE_TABLE", "service_usage")

MONGO_URI = os.getenv("DATA_MINING_MONGO_URI")
MONGO_DB_AUTH = os.getenv("MONGO_DB_AUTH")
MONGO_AUTH_COLLECTION = os.getenv("MONGO_AUTH_COLLECTION", "authentication_logs")

# Consumer & Saving Logic Configuration
SAVE_INTERVAL_SECONDS = int(os.getenv("SAVE_INTERVAL_SECONDS", 300)) # e.g., 5 minutes
MAX_BATCH_SIZE_TRANS = int(os.getenv("MAX_BATCH_SIZE_TRANS", 100))
MAX_BATCH_SIZE_AUTH = int(os.getenv("MAX_BATCH_SIZE_AUTH", 200))
# Add a maximum time to hold messages even if batch size isn't reached but interval also not met
MAX_HOLD_TIME_SECONDS = int(os.getenv("MAX_HOLD_TIME_SECONDS", 600)) # e.g. 10 minutes max hold

# SQLAlchemy connection string for MySQL
# Ensure the correct driver is specified if not mysqlconnector (e.g., pymysql)
# Syntax: dialect+driver://username:password@host:port/database
SQLALCHEMY_DATABASE_URI = f"mysql+mysqlconnector://{MYSQL_USER}:{MYSQL_PWD}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB_ACTIVITIES}"
POOL_RECYCLE = os.getenv("POOL_RECYCLE")

# --- 2. Global Variables & Accumulators ---
logging.info("Initializing accumulators...")
accumulated_transactions = deque()
accumulated_auth_logs = deque()
last_save_time = time.time()
first_message_time_trans = None
first_message_time_auth = None

# --- 3. Helper Functions ---

def create_sqlalchemy_engine():
    """Creates a SQLAlchemy engine."""
    try:
        engine = create_engine(SQLALCHEMY_DATABASE_URI, pool_recycle=POOL_RECYCLE) # Recycle connections hourly
        # Test connection - this will raise an error if connection fails
        with engine.connect() as connection:
            logging.info("SQLAlchemy engine created and connection tested successfully.")
        return engine
    except Exception as e:
        logging.error(f"Failed to create SQLAlchemy engine or test connection: {e}")
        return None

def get_mongo_client():
    """Creates a PyMongo client."""
    try:
        client = pymongo.MongoClient(MONGO_URI, serverSelectionTimeoutMS=5000) # 5 second timeout
        # The ismaster command is cheap and does not require auth.
        client.admin.command('ismaster')
        logging.info("PyMongo client created and connection tested successfully.")
        return client
    except pymongo.errors.ConnectionFailure as e:
        logging.error(f"Failed to connect to MongoDB: {e}")
        return None
    except Exception as e:
        logging.error(f"An unexpected error occurred connecting to MongoDB: {e}")
        return None

def parse_message(msg_body_raw):
    """Safely parses JSON message body."""
    try:
        msg_string = msg_body_raw.decode('utf-8') # Decode bytes to string
        parsed_dict = json.loads(msg_string)
        return parsed_dict
    except json.JSONDecodeError as e:
        logging.error(f"Error parsing JSON message: {e}. Message body (first 100 chars): '{msg_body_raw[:100]}...'")
        return None
    except UnicodeDecodeError as e:
        logging.error(f"Error decoding message body (not UTF-8?): {e}. Message body (first 100 bytes): '{msg_body_raw[:100]}...'")
        return None
    except Exception as e:
        logging.error(f"Unexpected error parsing message: {e}")
        return None

def prepare_data_for_sql(data_list, expected_cols):
    """Converts list of dicts to Pandas DataFrame, selects/adds expected cols."""
    if not data_list:
        return pd.DataFrame(columns=expected_cols)

    try:
        df = pd.DataFrame.from_records(list(data_list)) # Convert deque to list first

        # Add missing expected columns with None (which Pandas handles well)
        for col in expected_cols:
            if col not in df.columns:
                df[col] = None

        # Select only expected columns in the correct order
        df = df[expected_cols]

        # --- Crucial Type Conversions (Adjust based on our schemas) ---
        # Example conversions:
        if 'transaction_timestamp' in df.columns:
            df['transaction_timestamp'] = pd.to_datetime(df['transaction_timestamp'], errors='coerce')
        if 'client_id' in df.columns:
            # Use Int64 (nullable integer) if NAs are possible, else float/object might result
            df['client_id'] = pd.to_numeric(df['client_id'], errors='coerce').astype('Int64')
        if 'product_id' in df.columns:
            df['product_id'] = pd.to_numeric(df['product_id'], errors='coerce').astype('Int64')
        if 'service_id' in df.columns:
            df['service_id'] = pd.to_numeric(df['service_id'], errors='coerce').astype('Int64')
        if 'quantity' in df.columns:
            df['quantity'] = pd.to_numeric(df['quantity'], errors='coerce').astype('Int64')
        if 'unit_price' in df.columns:
            df['unit_price'] = pd.to_numeric(df['unit_price'], errors='coerce')
        if 'total_amount' in df.columns:
            df['total_amount'] = pd.to_numeric(df['total_amount'], errors='coerce')
        # Add more conversions for payment_status, delivery_status etc. if needed

        # Handle potential NaT (Not a Time) from timestamp conversion if column must not be NULL
        # Handle potential NA from numeric conversion if column must not be NULL

        return df

    except Exception as e:
        logging.error(f"Error preparing data for SQL: {e}")
        # Log the first few problematic records if possible
        try:
            logging.error(f"Problematic data sample: {list(data_list)[:2]}")
        except:
            pass # Ignore errors during error logging
        return pd.DataFrame(columns=expected_cols) # Return empty df on error

def prepare_data_for_mongo(data_list):
    """Prepares list of dicts for MongoDB insertion (basic type checks/conversion)."""
    if not data_list:
        return []
    # MongoDB is flexible, but converting timestamps is good practice
    processed_list = []
    for record in data_list:
        if isinstance(record, dict):
            # Example: Convert timestamp string to datetime object if present
            if 'timestamp' in record and isinstance(record['timestamp'], str):
                try:
                    # Attempt to parse common formats, adjust as needed
                    record['timestamp'] = pd.to_datetime(record['timestamp'], errors='coerce')
                    # Convert Pandas NaT to None, leave valid datetimes
                    if pd.isna(record['timestamp']): record['timestamp'] = None
                except Exception:
                    logging.warning(f"Could not parse timestamp in Mongo record: {record.get('timestamp')}")
                    record['timestamp'] = None # Or keep original string if preferred on error
            # Add other type checks/conversions if needed
            processed_list.append(record)
        else:
            logging.warning(f"Skipping non-dictionary item found in auth log accumulator: {record}")

    return processed_list

# --- 4. Save Function ---

def save_accumulated_data(sql_engine, mongo_client):
    """Saves accumulated data to databases."""
    global accumulated_transactions, accumulated_auth_logs, last_save_time
    global first_message_time_trans, first_message_time_auth # Need to reset these

    logging.info("Attempting to save accumulated data...")
    data_saved_transactions = False
    data_saved_auth = False

    # --- Process and Save Transactions (MySQL) ---
    local_transactions = None
    if accumulated_transactions:
        local_transactions = list(accumulated_transactions) # Copy deque to list
        accumulated_transactions.clear() # Clear the deque
        first_message_time_trans = None # Reset hold timer
        logging.info(f"Copied {len(local_transactions)} transaction messages for saving.")

    if local_transactions:
        if sql_engine is None:
            logging.error("Cannot save transactions: SQLAlchemy engine is not available.")
            # Decide on error handling: put back, save to file, or discard
            logging.warning("Discarding transaction batch due to missing DB connection.")
            # To retry: accumulated_transactions.extend(local_transactions) # Needs care!
        else:
            # Define expected columns based on your MySQL 'transactions' table
            expected_trans_cols = [
                "client_id", "product_id", "service_id", "quantity",
                "unit_price", "transaction_timestamp", "payment_method",
                "payment_status", "delivery_status", "delivery_time_days",
                "source_system", "total_amount" # Ensure this matches your schema exactly
            ]
            trans_df = prepare_data_for_sql(local_transactions, expected_trans_cols)

            if not trans_df.empty:
                logging.info(f"Attempting to save {len(trans_df)} processed transaction rows to MySQL table '{MYSQL_TRANSACTIONS_TABLE}'.")
                try:
                    # Use Pandas to_sql with SQLAlchemy engine
                    trans_df.to_sql(
                        name=MYSQL_TRANSACTIONS_TABLE,
                        con=sql_engine,
                        if_exists='append', # Append data
                        index=False,      # Don't write pandas index
                        chunksize=1000    # Optional: Insert in chunks for large batches
                    )
                    data_saved_transactions = True
                    logging.info("Successfully saved transaction data to MySQL.")
                except Exception as e:
                    logging.error(f"Error saving transaction data to MySQL: {e}")
                    # Robust error handling: Save trans_df to a CSV/JSON file for later manual inspection/retry
                    try:
                        fail_time = time.strftime("%Y%m%d_%H%M%S")
                        fail_file = f"failed_transactions_{fail_time}.json"
                        with open(fail_file, 'w') as f:
                            json.dump(local_transactions, f) # Save original list
                        logging.info(f"Saved failed transaction batch to {fail_file}")
                    except Exception as ef:
                        logging.error(f"Could not save failed transaction batch to file: {ef}")
            else:
                logging.warning("No valid transaction data rows to save after preparation.")
                data_saved_transactions = True # Nothing to save, effectively "saved"
    else:
        logging.info("No new transaction data accumulated to save.")
        data_saved_transactions = True # Nothing to save

    # --- Process and Save Auth Logs (MongoDB) ---
    local_auth_logs = None
    if accumulated_auth_logs:
        local_auth_logs = list(accumulated_auth_logs) # Copy deque to list
        accumulated_auth_logs.clear() # Clear the deque
        first_message_time_auth = None # Reset hold timer
        logging.info(f"Copied {len(local_auth_logs)} auth log messages for saving.")

    if local_auth_logs:
        if mongo_client is None:
            logging.error("Cannot save auth logs: MongoDB client is not available.")
            logging.warning("Discarding auth log batch due to missing DB connection.")
            # To retry: accumulated_auth_logs.extend(local_auth_logs)
        else:
            auth_list_processed = prepare_data_for_mongo(local_auth_logs)

            if auth_list_processed:
                logging.info(f"Attempting to save {len(auth_list_processed)} processed auth log documents to MongoDB collection '{MONGO_AUTH_COLLECTION}'.")
                try:
                    db = mongo_client[MONGO_DB_AUTH]
                    collection = db[MONGO_AUTH_COLLECTION]
                    # Insert the list of dictionaries
                    result = collection.insert_many(auth_list_processed, ordered=False) # ordered=False continues on error
                    data_saved_auth = True
                    logging.info(f"Successfully saved {len(result.inserted_ids)} auth log documents to MongoDB.")
                except pymongo.errors.BulkWriteError as bwe:
                    logging.error(f"Error saving auth log data to MongoDB (BulkWriteError): {bwe.details}")
                    # Details contain info about which documents failed
                    data_saved_auth = False # Consider partially successful? For simplicity, mark as failed.
                    # Save failed batch to file is recommended here too
                except Exception as e:
                    logging.error(f"Error saving auth log data to MongoDB: {e}")
                    # Save failed batch to file
                    try:
                        fail_time = time.strftime("%Y%m%d_%H%M%S")
                        fail_file = f"failed_auth_{fail_time}.json"
                        with open(fail_file, 'w') as f:
                            json.dump(local_auth_logs, f) # Save original list
                        logging.info(f"Saved failed auth log batch to {fail_file}")
                    except Exception as ef:
                        logging.error(f"Could not save failed auth log batch to file: {ef}")
            else:
                logging.warning("No valid auth log documents to save after preparation.")
                data_saved_auth = True # Nothing to save
    else:
        logging.info("No new auth log data accumulated to save.")
        data_saved_auth = True # Nothing to save

    # Update last save time only if both were successful or had nothing to save
    if data_saved_transactions and data_saved_auth:
        last_save_time = time.time()
        logging.info(f"Data saving cycle complete. Last save time updated to {last_save_time:.0f}")
    else:
        logging.warning("Data saving cycle completed with errors. Last save time NOT updated.")

# --- 5. RabbitMQ Callback Function ---

def callback(ch, method, properties, body):
    """Processes messages received from RabbitMQ."""
    global accumulated_transactions, accumulated_auth_logs
    global first_message_time_trans, first_message_time_auth

    delivery_tag = method.delivery_tag
    queue_name = method.routing_key # Assuming routing key is used as queue name, or adjust based on exchange type

    # logging.info(f"Received message from queue '{queue_name}' (tag: {delivery_tag})")

    parsed_data = parse_message(body)

    acked = False # Flag to ensure ack/nack happens

    if parsed_data is not None:
        try:
            if queue_name == TRANSACTIONS_QUEUE:
                accumulated_transactions.append(parsed_data)
                if first_message_time_trans is None: first_message_time_trans = time.time()
                # logging.info(f"Added to transactions queue. Size: {len(accumulated_transactions)}")
            elif queue_name == AUTH_LOGS_QUEUE:
                accumulated_auth_logs.append(parsed_data)
                if first_message_time_auth is None: first_message_time_auth = time.time()
                # logging.info(f"Added to auth logs queue. Size: {len(accumulated_auth_logs)}")
            else:
                logging.warning(f"Message received from unexpected queue/routing_key '{queue_name}'. Discarding.")
                # Acknowledge to remove from queue
                ch.basic_ack(delivery_tag=delivery_tag)
                acked = True
                return # Skip further processing

            # Acknowledge after successfully adding to deque
            if not acked:
                ch.basic_ack(delivery_tag=delivery_tag)
                acked = True
                # logging.info(f"Acknowledged message tag {delivery_tag}")

        except Exception as e:
            logging.error(f"Error processing message tag {delivery_tag} after parsing: {e}")
            # Decide whether to Nack (requeue) or Ack (discard) based on error type
            # For most processing errors after parsing, discard to avoid infinite loops
            if not acked:
                try:
                    ch.basic_ack(delivery_tag=delivery_tag) # Discard on error
                    logging.warning(f"Acknowledged message tag {delivery_tag} after processing error (discarded).")
                    acked = True
                except Exception as ack_err:
                    logging.error(f"Failed to acknowledge message tag {delivery_tag} after processing error: {ack_err}")
    else:
        # Parsing failed
        logging.warning(f"Discarding unparseable message tag {delivery_tag}.")
        if not acked:
            try:
                ch.basic_ack(delivery_tag=delivery_tag) # Discard unparseable message
                acked = True
            except Exception as ack_err:
                logging.error(f"Failed to acknowledge unparseable message tag {delivery_tag}: {ack_err}")

    # Final check if acknowledgement failed somehow
    if not acked:
        logging.critical(f"Message tag {delivery_tag} was processed but acknowledgement logic failed!")
        # This indicates a code path error, requires investigation. Message might be redelivered.


# --- 6. Main Function ---
def main():
    """Main connection and consumption loop."""
    global last_save_time, first_message_time_trans, first_message_time_auth # Allow modification

    logging.info("Starting SNS Consumer Service...")
    sql_engine = create_sqlalchemy_engine() # Create engine once
    mongo_client = get_mongo_client() # Create client once

    connection = None
    while True: # Outer loop for handling AMQP connection errors
        try:
            logging.info(f"Attempting to connect to RabbitMQ at {AMQP_HOST}:{AMQP_PORT}...")
            credentials = pika.PlainCredentials(AMQP_USER, AMQP_PWD)
            parameters = pika.ConnectionParameters(host=AMQP_HOST,
                                                   port=AMQP_PORT,
                                                   virtual_host=AMQP_VHOST,
                                                   credentials=credentials,
                                                   heartbeat=600, # Increase heartbeat
                                                   blocked_connection_timeout=300) # Timeout
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()
            logging.info("RabbitMQ connection successful. Setting up queues and consumers.")

            # Declare queues (idempotent, durable)
            channel.queue_declare(queue=TRANSACTIONS_QUEUE, durable=True)
            logging.info(f"Declared queue: {TRANSACTIONS_QUEUE}")
            channel.queue_declare(queue=AUTH_LOGS_QUEUE, durable=True)
            logging.info(f"Declared queue: {AUTH_LOGS_QUEUE}")

            # Set QoS (Quality of Service) - process only N messages at a time
            # Helps prevent overwhelming the consumer if messages pile up
            channel.basic_qos(prefetch_count=max(MAX_BATCH_SIZE_TRANS, MAX_BATCH_SIZE_AUTH) * 2) # Example: Prefetch ~2 batches

            # Setup consumers
            channel.basic_consume(queue=TRANSACTIONS_QUEUE,
                                  on_message_callback=callback,
                                  auto_ack=False) # Manual acknowledgement
            channel.basic_consume(queue=AUTH_LOGS_QUEUE,
                                  on_message_callback=callback,
                                  auto_ack=False) # Manual acknowledgement

            logging.info("Consumers started. Waiting for messages...")
            # Start the blocking consumer loop - this runs until connection error/closure
            # We need a way to periodically check save conditions without blocking this loop entirely.
            # pika's BlockingConnection isn't ideal for mixing timed tasks easily.
            # Workaround: Use connection.process_data_events with a timeout.

            while True: # Inner loop to process events and check save conditions
                # Process events for a short time, then check save conditions
                connection.process_data_events(time_limit=1) # Process events for 1 second

                # Check save conditions
                current_time = time.time()
                time_condition = (current_time - last_save_time) >= SAVE_INTERVAL_SECONDS
                batch_condition_trans = len(accumulated_transactions) >= MAX_BATCH_SIZE_TRANS
                batch_condition_auth = len(accumulated_auth_logs) >= MAX_BATCH_SIZE_AUTH
                # Check max hold time
                hold_time_exceeded_trans = (first_message_time_trans is not None and
                                            (current_time - first_message_time_trans) >= MAX_HOLD_TIME_SECONDS)
                hold_time_exceeded_auth = (first_message_time_auth is not None and
                                           (current_time - first_message_time_auth) >= MAX_HOLD_TIME_SECONDS)


                if time_condition or batch_condition_trans or batch_condition_auth or hold_time_exceeded_trans or hold_time_exceeded_auth:
                    log_msg = "Save triggered by: "
                    if time_condition: log_msg += "[Interval]"
                    if batch_condition_trans: log_msg += "[Trans Batch Size]"
                    if batch_condition_auth: log_msg += "[Auth Batch Size]"
                    if hold_time_exceeded_trans: log_msg += "[Trans Max Hold]"
                    if hold_time_exceeded_auth: log_msg += "[Auth Max Hold]"
                    logging.info(log_msg)

                    # Check DB connections before saving
                    if sql_engine is None: sql_engine = create_sqlalchemy_engine() # Recreate if missing
                    if mongo_client is None: mongo_client = get_mongo_client() # Recreate if missing

                    save_accumulated_data(sql_engine, mongo_client)
                    # last_save_time updated inside function on success


        except pika.exceptions.AMQPConnectionError as e:
            logging.error(f"RabbitMQ Connection Error: {e}. Retrying in 10 seconds...")
        except pika.exceptions.AMQPChannelError as e:
            logging.error(f"RabbitMQ Channel Error: {e}. Retrying connection in 10 seconds...")
        except KeyboardInterrupt:
            logging.info("Ctrl+C received. Shutting down consumer...")
            break # Exit the outer while loop
        except Exception as e:
            logging.error(f"An unexpected error occurred in the main loop: {e}", exc_info=True) # Log traceback
            logging.error("Retrying connection in 10 seconds...")
        finally:
            if connection and connection.is_open:
                try:
                    logging.info("Closing RabbitMQ connection.")
                    connection.close()
                except Exception as ce:
                    logging.error(f"Error closing RabbitMQ connection: {ce}")
            connection = None # Ensure connection is reset for retry loop
            # Don't close DB connections here, let them be potentially reused or recreated

        time.sleep(10) # Wait before retrying connection

    logging.info("SNS Consumer Service stopped.")
    # Clean up DB connections on final exit
    if sql_engine:
        try:
            sql_engine.dispose()
            logging.info("SQLAlchemy engine disposed.")
        except Exception as e:
            logging.error(f"Error disposing SQLAlchemy engine: {e}")
    if mongo_client:
        try:
            mongo_client.close()
            logging.info("MongoDB client closed.")
        except Exception as e:
            logging.error(f"Error closing MongoDB client: {e}")


# --- 7. Script Execution ---
if __name__ == "__main__":
    main()