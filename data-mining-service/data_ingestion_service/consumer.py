# consumer.py
# RabbitMQ Consumer for SNS Data Ingestion
# Corrected version

import pika
import os
import time
import json
import logging
import sys
from collections import deque

# --- Database Imports ---
import pymongo
import mysql.connector
from sqlalchemy import create_engine
import pandas as pd

# --- Setup Logging ---
log_level_str = os.getenv("LOG_LEVEL", "info").upper()
log_level = getattr(logging, log_level_str, logging.INFO)
logging.basicConfig(level=log_level,
                    format='%(asctime)s - %(levelname)s - %(message)s',
                    stream=sys.stdout)

# --- 1. Configuration from Environment Variables ---
logging.info("Loading configuration from environment variables set by Docker Compose...")

# RabbitMQ Configuration
AMQP_HOST = os.getenv("RABBITMQ_HOST", "rabbitmq")
AMQP_PORT = int(os.getenv("RABBITMQ_PORT", 5672))
AMQP_USER = os.getenv("RABBITMQ_USER")
AMQP_PWD = os.getenv("RABBITMQ_PASSWORD")
AMQP_VHOST = os.getenv("AMQP_VHOST", "/")

if not AMQP_USER or not AMQP_PWD:
    logging.warning("RabbitMQ user or password environment variables not set!")

# Queue Names
ACTIVITY_LOGS_QUEUE = os.getenv("ACTIVITY_LOGS_QUEUE", "q_activity_logs")
AUTH_LOGS_QUEUE = os.getenv("AUTH_LOGS_QUEUE", "q.auth_logs")
logging.info(f"Listening on Activity Queue: {ACTIVITY_LOGS_QUEUE}")
logging.info(f"Listening on Auth Logs Queue: {AUTH_LOGS_QUEUE}")

# MySQL ROOT Database Configuration
MYSQL_USER = os.getenv("MYSQL_USER")
MYSQL_PWD = os.getenv("MYSQL_PWD")
MYSQL_HOST = os.getenv("MYSQL_HOST", "mariadb")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", 3306))
MYSQL_DB_ACTIVITIES = os.getenv("MYSQL_DB_ACTIVITIES")
MYSQL_TRANSACTIONS_TABLE = os.getenv("MYSQL_TRANSACTIONS_TABLE", "transactions")
MYSQL_SERVICE_USAGE_TABLE = os.getenv("MYSQL_SERVICE_USAGE_TABLE", "service_usage")
logging.info(f"Target MySQL Table (Transactions): {MYSQL_TRANSACTIONS_TABLE}")
logging.info(f"Target MySQL Table (Service Usage): {MYSQL_SERVICE_USAGE_TABLE}")

if not all([MYSQL_USER, MYSQL_PWD, MYSQL_HOST, MYSQL_PORT, MYSQL_DB_ACTIVITIES]):
    logging.error("One or more MySQL environment variables are missing!")
logging.info(f"Target MySQL DB: {MYSQL_DB_ACTIVITIES} on {MYSQL_HOST}:{MYSQL_PORT} as user '{MYSQL_USER}'")

# MongoDB ROOT Database Configuration
MONGO_USER = os.getenv("MONGO_USER")
MONGO_PWD = os.getenv("MONGO_PWD")
MONGO_HOST = os.getenv("MONGO_HOST", "mongodb")
MONGO_PORT = int(os.getenv("MONGO_PORT", 27017))
MONGO_DB_AUTH = os.getenv("MONGO_DB_AUTH")
MONGO_AUTH_SOURCE = os.getenv("MONGO_AUTH_SOURCE", "admin")
MONGO_AUTH_COLLECTION = os.getenv("MONGO_AUTH_COLLECTION", "authentication_logs")
logging.info(f"Target MongoDB Collection: {MONGO_AUTH_COLLECTION}")

if not all([MONGO_USER, MONGO_PWD, MONGO_HOST, MONGO_PORT, MONGO_DB_AUTH]):
    logging.error("One or more MongoDB environment variables are missing!")
logging.info(f"Target MongoDB: {MONGO_DB_AUTH} on {MONGO_HOST}:{MONGO_PORT} as user '{MONGO_USER}' (authSource={MONGO_AUTH_SOURCE})")

MONGO_URI_CONSTRUCTED = f"mongodb://{MONGO_USER}:{MONGO_PWD}@{MONGO_HOST}:{MONGO_PORT}/{MONGO_DB_AUTH}?authSource={MONGO_AUTH_SOURCE}"
logging.debug(f"Constructed Mongo URI: mongodb://{MONGO_USER}:***@{MONGO_HOST}:{MONGO_PORT}/{MONGO_DB_AUTH}?authSource={MONGO_AUTH_SOURCE}")

# Consumer & Saving Logic Configuration
SAVE_INTERVAL_SECONDS = int(os.getenv("SAVE_INTERVAL_SECONDS", 120))
MAX_BATCH_SIZE_TRANS = int(os.getenv("MAX_BATCH_SIZE_TRANS", 100))
MAX_BATCH_SIZE_AUTH = int(os.getenv("MAX_BATCH_SIZE_AUTH", 200))
# Added service batch size configuration
MAX_BATCH_SIZE_SERVICE = int(os.getenv("MAX_BATCH_SIZE_SERVICE", 100))
MAX_HOLD_TIME_SECONDS = int(os.getenv("MAX_HOLD_TIME_SECONDS", 600))
POOL_RECYCLE = int(os.getenv("POOL_RECYCLE", 3600))

logging.info(f"Save Interval: {SAVE_INTERVAL_SECONDS}s")
logging.info(f"Max Batch Sizes (Trans/Auth/Service): {MAX_BATCH_SIZE_TRANS}/{MAX_BATCH_SIZE_AUTH}/{MAX_BATCH_SIZE_SERVICE}")
logging.info(f"Max Hold Time: {MAX_HOLD_TIME_SECONDS}s")
logging.info(f"SQLAlchemy Pool Recycle: {POOL_RECYCLE}s")

# SQLAlchemy connection string
SQLALCHEMY_DATABASE_URI = f"mysql+mysqlconnector://{MYSQL_USER}:{MYSQL_PWD}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB_ACTIVITIES}"
logging.debug(f"Constructed SQLAlchemy URI: mysql+mysqlconnector://{MYSQL_USER}:***@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB_ACTIVITIES}")

# --- 2. Global Variables & Accumulators --- (Single correct block)
logging.info("Initializing accumulators...")
accumulated_transactions = deque()
accumulated_auth_logs = deque()
accumulated_service_usage = deque()
last_save_time = time.time()
first_message_time_trans = None
first_message_time_auth = None
first_message_time_service = None

# --- 3. Helper Functions ---

def create_sqlalchemy_engine():
    """Creates a SQLAlchemy engine using configured URI."""
    try:
        engine = create_engine(SQLALCHEMY_DATABASE_URI, pool_recycle=POOL_RECYCLE)
        with engine.connect() as connection:
            logging.info("SQLAlchemy engine created and connection tested successfully.")
        return engine
    except Exception as e:
        logging.error(f"Failed to create SQLAlchemy engine or test connection using URI '{SQLALCHEMY_DATABASE_URI}': {e}", exc_info=True)
        return None

def get_mongo_client():
    """Creates a PyMongo client using configured URI."""
    try:
        client = pymongo.MongoClient(MONGO_URI_CONSTRUCTED, serverSelectionTimeoutMS=5000)
        client.admin.command('ismaster') # Test connection
        logging.info("PyMongo client created and connection tested successfully.")
        return client
    except pymongo.errors.ConnectionFailure as e:
        logging.error(f"Failed to connect to MongoDB using URI '{MONGO_URI_CONSTRUCTED}': {e}", exc_info=True)
        return None
    except Exception as e:
        logging.error(f"An unexpected error occurred connecting to MongoDB using URI '{MONGO_URI_CONSTRUCTED}': {e}", exc_info=True)
        return None

def parse_message(msg_body_raw):
    """Safely parses JSON message body."""
    try:
        msg_string = msg_body_raw.decode('utf-8')
        parsed_dict = json.loads(msg_string)
        return parsed_dict
    except json.JSONDecodeError as e:
        logging.error(f"Error parsing JSON message: {e}. Body(100): '{msg_body_raw[:100]}...'")
        return None
    except UnicodeDecodeError as e:
        logging.error(f"Error decoding message (UTF-8): {e}. Body(100): '{msg_body_raw[:100]}...'")
        return None
    except Exception as e:
        logging.error(f"Unexpected error parsing message: {e}")
        return None

def prepare_data_for_sql(data_list, expected_cols):
    """Converts list of dicts to Pandas DataFrame, selects/adds expected cols."""
    if not data_list:
        return pd.DataFrame(columns=expected_cols)
    try:
        # Convert deque to list if necessary before DataFrame creation
        if isinstance(data_list, deque):
            data_list = list(data_list)

        df = pd.DataFrame.from_records(data_list)
        # Add missing columns and ensure correct order
        for col in expected_cols:
            if col not in df.columns:
                df[col] = None
        df = df[expected_cols]

        # --- Type Conversions ---
        time_cols = ['transaction_timestamp', 'usage_timestamp']
        for col in time_cols:
            if col in df.columns:
                # Use errors='coerce' to turn unparseable dates into NaT (Not a Time)
                df[col] = pd.to_datetime(df[col], errors='coerce')

        int_cols = ['client_id', 'product_id', 'service_id', 'quantity', 'delivery_time_days', 'duration_ms']
        for col in int_cols:
            if col in df.columns:
                # Use Int64 (nullable integer type)
                df[col] = pd.to_numeric(df[col], errors='coerce').astype('Int64')

        float_cols = ['unit_price', 'total_amount']
        for col in float_cols:
            if col in df.columns:
                df[col] = pd.to_numeric(df[col], errors='coerce')

        # Handle JSON/Text fields if needed (example for request_details)
        if 'request_details' in df.columns:
            df['request_details'] = df['request_details'].apply(lambda x: json.dumps(x) if isinstance(x, (dict, list)) else x)


        # Handle potential NaNs/NaTs in non-nullable DB columns before writing if required
        # (e.g., replace NaNs with default values or filter rows)

        return df
    except Exception as e:
        logging.error(f"Error preparing data for SQL: {e}", exc_info=True)
        try:
            logging.error(f"Problematic data sample: {list(data_list)[:2]}")
        except: pass
        return pd.DataFrame(columns=expected_cols)

def prepare_data_for_mongo(data_list):
    """Prepares list of dicts for MongoDB insertion (basic type checks/conversion)."""
    if not data_list: return []
    processed_list = []
    for record in data_list:
        if isinstance(record, dict):
            # Convert timestamp string to datetime object if present and a string
            if 'timestamp' in record and isinstance(record['timestamp'], str):
                try:
                    dt_obj = pd.to_datetime(record['timestamp'], errors='coerce')
                    if not pd.isna(dt_obj):
                        record['timestamp'] = dt_obj.to_pydatetime()
                    else:
                        record['timestamp'] = None
                except Exception as e:
                    logging.warning(f"Could not parse timestamp '{record.get('timestamp')}' in Mongo record: {e}")
                    record['timestamp'] = None
            # Convert client_id to int if it's a numeric string? Optional.
            if 'client_id' in record and isinstance(record['client_id'], str) and record['client_id'].isdigit():
                record['client_id'] = int(record['client_id'])

            processed_list.append(record)
        else:
            logging.warning(f"Skipping non-dictionary item in auth log accumulator: {record}")
    return processed_list


# --- 4. Save Function --- (Corrected version)

def save_accumulated_data(sql_engine, mongo_client, mysql_trans_table, mongo_auth_collection, mysql_service_table):
    """Saves accumulated data to databases."""
    global accumulated_transactions, accumulated_auth_logs, accumulated_service_usage, last_save_time
    global first_message_time_trans, first_message_time_auth, first_message_time_service

    logging.info("Attempting to save accumulated data...")
    data_saved_transactions = False
    data_saved_auth = False
    data_saved_service = False
    current_time_save_start = time.time()

    # --- Process and Save Transactions (MySQL) ---
    local_transactions = None
    if accumulated_transactions:
        local_transactions = list(accumulated_transactions)
        accumulated_transactions.clear()
        first_message_time_trans = None
        logging.info(f"Processing {len(local_transactions)} transaction messages for saving.")

    if local_transactions:
        if sql_engine is None:
            logging.error("Cannot save transactions: SQLAlchemy engine is unavailable.")
            logging.warning("Discarding transaction batch due to missing DB connection.")
        else:
            expected_trans_cols = [
                "client_id", "product_id", "service_id", "quantity",
                "unit_price", "transaction_timestamp", "payment_method",
                "payment_status", "delivery_status", "delivery_time_days",
                "source_system", "total_amount" # Add/Remove cols to match your DB schema
            ]
            trans_df = prepare_data_for_sql(local_transactions, expected_trans_cols)

            if not trans_df.empty:
                logging.info(f"Attempting to save {len(trans_df)} transaction rows to MySQL table '{mysql_trans_table}'.")
                try:
                    trans_df.to_sql(
                        name=mysql_trans_table,
                        con=sql_engine,
                        if_exists='append',
                        index=False,
                        chunksize=500
                    )
                    data_saved_transactions = True
                    logging.info(f"Successfully saved transaction data to MySQL.") # Removed duration here for overall timing
                except Exception as e:
                    logging.error(f"Error saving transaction data to MySQL: {e}", exc_info=True)
                    try:
                        fail_time = time.strftime("%Y%m%d_%H%M%S")
                        fail_file = f"failed_transactions_{fail_time}.json"
                        with open(fail_file, 'w') as f:
                            json.dump(local_transactions, f, default=str)
                        logging.info(f"Saved failed transaction batch to {fail_file}")
                    except Exception as ef:
                        logging.error(f"Could not save failed transaction batch to file: {ef}")
            else:
                logging.warning("No valid transaction data rows to save after preparation.")
                data_saved_transactions = True
    else:
        logging.info("No new transaction data accumulated to save.")
        data_saved_transactions = True

    # --- Process and Save Service Usage (MySQL) ---
    local_service_usage = None
    if accumulated_service_usage:
        local_service_usage = list(accumulated_service_usage)
        accumulated_service_usage.clear()
        first_message_time_service = None
        logging.info(f"Processing {len(local_service_usage)} service usage messages for saving.")

    if local_service_usage:
        if sql_engine is None:
            logging.error("Cannot save service usage: SQLAlchemy engine is unavailable.")
            logging.warning("Discarding service usage batch due to missing DB connection.")
        else:
            expected_service_cols = [
                "client_id", "service_id", "usage_timestamp",
                "request_details", "status", "duration_ms"
                # Add "usage_log_id_source" if you have this column
            ]
            service_df = prepare_data_for_sql(local_service_usage, expected_service_cols)

            if not service_df.empty:
                logging.info(f"Attempting to save {len(service_df)} service usage rows to MySQL table '{mysql_service_table}'.")
                try:
                    service_df.to_sql(
                        name=mysql_service_table,
                        con=sql_engine,
                        if_exists='append',
                        index=False,
                        chunksize=500
                    )
                    data_saved_service = True
                    logging.info(f"Successfully saved service usage data to MySQL.")
                except Exception as e:
                    logging.error(f"Error saving service usage data to MySQL: {e}", exc_info=True)
                    try:
                        fail_time = time.strftime("%Y%m%d_%H%M%S")
                        fail_file = f"failed_service_usage_{fail_time}.json"
                        with open(fail_file, 'w') as f:
                            json.dump(local_service_usage, f, default=str)
                        logging.info(f"Saved failed service usage batch to {fail_file}")
                    except Exception as ef:
                        logging.error(f"Could not save failed service usage batch to file: {ef}")
            else:
                logging.warning("No valid service usage data rows to save after preparation.")
                data_saved_service = True
    else:
        logging.info("No new service usage data accumulated to save.")
        data_saved_service = True

    # --- Process and Save Auth Logs (MongoDB) ---
    current_time_auth_save_start = time.time() # Specific timing for this part
    local_auth_logs = None
    if accumulated_auth_logs:
        local_auth_logs = list(accumulated_auth_logs)
        accumulated_auth_logs.clear()
        first_message_time_auth = None
        logging.info(f"Processing {len(local_auth_logs)} auth log messages for saving.")

    if local_auth_logs:
        if mongo_client is None:
            logging.error("Cannot save auth logs: MongoDB client is unavailable.")
            logging.warning("Discarding auth log batch due to missing DB connection.")
        else:
            auth_list_processed = prepare_data_for_mongo(local_auth_logs)
            if auth_list_processed:
                # *** CORRECTED USAGE OF ARGUMENT NAME ***
                logging.info(f"Attempting to save {len(auth_list_processed)} processed auth log documents to MongoDB collection '{mongo_auth_collection}'.")
                try:
                    db = mongo_client[MONGO_DB_AUTH]
                    # *** CORRECTED USAGE OF ARGUMENT NAME ***
                    collection = db[mongo_auth_collection]
                    result = collection.insert_many(auth_list_processed, ordered=False)
                    data_saved_auth = True
                    logging.info(f"Successfully saved {len(result.inserted_ids)} auth log documents to MongoDB. Duration: {time.time() - current_time_auth_save_start:.2f}s")
                except pymongo.errors.BulkWriteError as bwe:
                    logging.error(f"Error saving auth log data to MongoDB (BulkWriteError): {bwe.details}")
                    data_saved_auth = False
                except Exception as e:
                    logging.error(f"Error saving auth log data to MongoDB: {e}", exc_info=True)
                    data_saved_auth = False
                    # Consider saving failed batch here too
            else:
                logging.warning("No valid auth log documents to save after preparation.")
                data_saved_auth = True
    else:
        logging.info("No new auth log data accumulated to save.")
        data_saved_auth = True

    # Update last save time only if ALL operations were successful
    if data_saved_transactions and data_saved_auth and data_saved_service:
        last_save_time = time.time()
        logging.info(f"Data saving cycle complete. Last save time updated. Total duration: {time.time() - current_time_save_start:.2f}s")
    else:
        logging.warning("Data saving cycle completed with errors. Last save time NOT updated.")


# --- 5. RabbitMQ Callback Function ---

def callback(ch, method, properties, body):
    """Processes messages received from RabbitMQ."""
    global accumulated_transactions, accumulated_auth_logs, accumulated_service_usage
    global first_message_time_trans, first_message_time_auth, first_message_time_service

    delivery_tag = method.delivery_tag
    queue_name = method.routing_key
    logging.debug(f"Received message from queue '{queue_name}' (tag: {delivery_tag})")

    parsed_data = parse_message(body)
    acked = False

    if parsed_data is not None:
        payload = parsed_data.get('payload', parsed_data)
        event_type = parsed_data.get('eventType', None)

        try:
            if queue_name == ACTIVITY_LOGS_QUEUE:
                if event_type == "TransactionCompleted":
                    accumulated_transactions.append(payload)
                    if first_message_time_trans is None: first_message_time_trans = time.time()
                    logging.debug(f"Added to transactions. Size: {len(accumulated_transactions)}")
                elif event_type == "ServiceUsed":
                    accumulated_service_usage.append(payload)
                    if first_message_time_service is None: first_message_time_service = time.time()
                    logging.debug(f"Added to service usage. Size: {len(accumulated_service_usage)}")
                else:
                    logging.warning(f"Received unknown eventType '{event_type}' on queue '{ACTIVITY_LOGS_QUEUE}'. Discarding tag {delivery_tag}.")

            elif queue_name == AUTH_LOGS_QUEUE:
                accumulated_auth_logs.append(payload)
                if first_message_time_auth is None: first_message_time_auth = time.time()
                logging.debug(f"Added to auth logs. Size: {len(accumulated_auth_logs)}")
            else:
                logging.warning(f"Message received from unexpected queue/routing_key '{queue_name}'. Discarding tag {delivery_tag}.")

            # Ack only if message was handled or intentionally discarded above
            ch.basic_ack(delivery_tag=delivery_tag)
            acked = True
            logging.debug(f"Acknowledged message tag {delivery_tag}")

        except Exception as e:
            logging.error(f"Error processing message tag {delivery_tag} after parsing: {e}", exc_info=True)
            if not acked:
                try:
                    ch.basic_ack(delivery_tag=delivery_tag)
                    logging.warning(f"Acknowledged message tag {delivery_tag} after processing error (discarded).")
                    acked = True
                except Exception as ack_err:
                    logging.error(f"Failed to acknowledge message tag {delivery_tag} after processing error: {ack_err}")
    else:
        logging.warning(f"Discarding unparseable message tag {delivery_tag}.")
        if not acked:
            try:
                ch.basic_ack(delivery_tag=delivery_tag)
                acked = True
            except Exception as ack_err:
                logging.error(f"Failed to acknowledge unparseable message tag {delivery_tag}: {ack_err}")

    if not acked:
        logging.critical(f"Message tag {delivery_tag} processed but ACK/NACK logic failed!")


# --- 6. Main Function ---

def main():
    """Main connection and consumption loop."""
    # Need to declare service usage timer global
    global last_save_time, first_message_time_trans, first_message_time_auth, first_message_time_service

    logging.info("Starting SNS Consumer Service...")
    sql_engine = create_sqlalchemy_engine()
    mongo_client = get_mongo_client()

    connection = None
    while True:
        try:
            logging.info(f"Attempting RabbitMQ connection: {AMQP_USER}@{AMQP_HOST}:{AMQP_PORT}{AMQP_VHOST}")
            credentials = pika.PlainCredentials(AMQP_USER, AMQP_PWD)
            parameters = pika.ConnectionParameters(
                host=AMQP_HOST,
                port=AMQP_PORT,
                virtual_host=AMQP_VHOST,
                credentials=credentials,
                heartbeat=120,
                blocked_connection_timeout=300)
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()
            logging.info("RabbitMQ connection successful.")

            channel.queue_declare(queue=ACTIVITY_LOGS_QUEUE, durable=True)
            logging.info(f"Ensured queue exists: {ACTIVITY_LOGS_QUEUE}")
            channel.queue_declare(queue=AUTH_LOGS_QUEUE, durable=True)
            logging.info(f"Ensured queue exists: {AUTH_LOGS_QUEUE}")

            qos_prefetch = max(MAX_BATCH_SIZE_TRANS, MAX_BATCH_SIZE_AUTH, MAX_BATCH_SIZE_SERVICE) * 2
            channel.basic_qos(prefetch_count=qos_prefetch)
            logging.info(f"QoS prefetch count set to {qos_prefetch}")

            channel.basic_consume(queue=ACTIVITY_LOGS_QUEUE, on_message_callback=callback, auto_ack=False)
            channel.basic_consume(queue=AUTH_LOGS_QUEUE, on_message_callback=callback, auto_ack=False)

            logging.info("Consumers started. Waiting for messages and checking save conditions...")

            while True:
                connection.process_data_events(time_limit=1.0)

                current_time = time.time()
                time_condition = (current_time - last_save_time) >= SAVE_INTERVAL_SECONDS
                batch_condition_trans = len(accumulated_transactions) >= MAX_BATCH_SIZE_TRANS
                batch_condition_auth = len(accumulated_auth_logs) >= MAX_BATCH_SIZE_AUTH
                batch_condition_service = len(accumulated_service_usage) >= MAX_BATCH_SIZE_SERVICE

                hold_time_exceeded_trans = (first_message_time_trans is not None and
                                            (current_time - first_message_time_trans) >= MAX_HOLD_TIME_SECONDS)
                hold_time_exceeded_auth = (first_message_time_auth is not None and
                                           (current_time - first_message_time_auth) >= MAX_HOLD_TIME_SECONDS)
                hold_time_exceeded_service = (first_message_time_service is not None and
                                              (current_time - first_message_time_service) >= MAX_HOLD_TIME_SECONDS)

                should_save = (time_condition or batch_condition_trans or batch_condition_auth or batch_condition_service
                               or hold_time_exceeded_trans or hold_time_exceeded_auth or hold_time_exceeded_service)

                if should_save and (accumulated_transactions or accumulated_auth_logs or accumulated_service_usage):
                    log_msg = "Save triggered by: "
                    if time_condition: log_msg += "[Interval]"
                    if batch_condition_trans: log_msg += "[Trans Batch Size]"
                    if batch_condition_auth: log_msg += "[Auth Batch Size]"
                    if batch_condition_service: log_msg += "[Service Batch Size]"
                    if hold_time_exceeded_trans: log_msg += "[Trans Max Hold]"
                    if hold_time_exceeded_auth: log_msg += "[Auth Max Hold]"
                    if hold_time_exceeded_service: log_msg += "[Service Max Hold]"
                    logging.info(log_msg)

                    # Check DB connections
                    if sql_engine is None:
                        logging.warning("SQLAlchemy engine was None. Attempting to recreate.")
                        sql_engine = create_sqlalchemy_engine()

                    if mongo_client is None:
                        logging.warning("Mongo client was None. Attempting to recreate.")
                        mongo_client = get_mongo_client()
                    else:
                        try:
                            mongo_client.admin.command('ismaster')
                        except Exception as mongo_err:
                            logging.error(f"MongoDB connection test failed: {mongo_err}. Attempting to recreate client.")
                            try: mongo_client.close()
                            except: pass
                            mongo_client = get_mongo_client()

                    # Call save function with all necessary arguments
                    save_accumulated_data(sql_engine, mongo_client,
                                          MYSQL_TRANSACTIONS_TABLE,
                                          MONGO_AUTH_COLLECTION,
                                          MYSQL_SERVICE_USAGE_TABLE)

        except pika.exceptions.AMQPConnectionError as e:
            logging.error(f"RabbitMQ Connection Error: {e}. Retrying in 15 seconds...")
            time.sleep(15)
        except pika.exceptions.AMQPChannelError as e:
            logging.error(f"RabbitMQ Channel Error: {e}. Retrying connection in 15 seconds...")
            time.sleep(15)
        except KeyboardInterrupt:
            logging.info("Ctrl+C received. Shutting down...")
            break
        except Exception as e:
            logging.error(f"An unexpected error occurred in the main loop: {e}", exc_info=True)
            logging.error("Retrying connection in 15 seconds...")
            time.sleep(15)
        finally:
            if connection and connection.is_open:
                try: connection.close()
                except Exception as ce: logging.error(f"Error closing RabbitMQ connection: {ce}")
            connection = None

    logging.info("SNS Consumer Service stopped.")
    if sql_engine:
        try: sql_engine.dispose()
        except Exception as e: logging.error(f"Error disposing SQLAlchemy engine: {e}")
    if mongo_client:
        try: mongo_client.close()
        except Exception as e: logging.error(f"Error closing MongoDB client: {e}")

# --- 7. Script Execution ---
if __name__ == "__main__":
    try:
        from dotenv import load_dotenv
        if load_dotenv():
            logging.info("Loaded environment variables from .env file.")
        else:
            logging.info("No .env file found or it is empty.")
    except ImportError:
        logging.info("python-dotenv not installed, skipping .env file loading.")

    main()