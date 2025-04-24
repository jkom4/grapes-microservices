# test_publisher.py
import pika
import json
import time
import os
import random
from dotenv import load_dotenv


load_dotenv()

# Use the same connection details as the consumer might use
AMQP_HOST = os.getenv("RABBITMQ_HOST") # Connect to localhost since script runs on host
AMQP_PORT = int(os.getenv("RABBITMQ_PORT")) # Use the HOST port mapped in compose
AMQP_USER = os.getenv("RABBITMQ_USER") # Use user from .env
AMQP_PWD = os.getenv("RABBITMQ_PASSWORD") # Use password from .env
AMQP_VHOST = os.getenv("AMQP_VHOST", "/")

ACTIVITY_LOGS_QUEUE = os.getenv("ACTIVITY_LOGS_QUEUE", "q_activity_logs")
AUTH_LOGS_QUEUE = os.getenv("AUTH_LOGS_QUEUE", "q.auth_logs")

def publish_message(channel, queue_name, message_body):
    try:
        channel.basic_publish(
            exchange='', # Default exchange
            routing_key=queue_name, # Send directly to the queue
            body=json.dumps(message_body), # Encode dict to JSON string
            properties=pika.BasicProperties(
                delivery_mode=2,  # make message persistent
            ))
        print(f" [x] Sent message to {queue_name}")
    except Exception as e:
        print(f" [!] Error publishing to {queue_name}: {e}")

try:
    credentials = pika.PlainCredentials(AMQP_USER, AMQP_PWD)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=AMQP_HOST, port=AMQP_PORT, virtual_host=AMQP_VHOST, credentials=credentials)
    )
    channel = connection.channel()

    # Ensure queues exist (optional, consumer does this too, but good practice)
    channel.queue_declare(queue=ACTIVITY_LOGS_QUEUE, durable=True)
    channel.queue_declare(queue=AUTH_LOGS_QUEUE, durable=True)

    # --- Send some test messages ---
    for i in range(5):
        # --- Transaction Message ---
        # 1. Create the inner payload first
        trans_payload = {
            "client_id": random.randint(1, 2000),
            "product_id": random.randint(1, 50),
            "service_id": None,
            "quantity": random.randint(1, 5),
            "unit_price": round(random.uniform(1.0, 20.0), 2),
            "transaction_timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ"), # Added Z for UTC example
            "payment_method": random.choice(["Visa", "Mastercard", "PayPal"]),
            "payment_status": "Success",
            "delivery_status": random.choice(["Shipped", "Processing", "Delivered"]),
            "delivery_time_days": random.randint(1, 7) if random.random() > 0.1 else None,
            "source_system": random.choice(["WebApp", "MobileApp"]),
            "total_amount": 0
            # Add other fields like currency if expected by consumer/DB
        }
        trans_payload["total_amount"] = round(trans_payload["quantity"] * trans_payload["unit_price"], 2)

        # 2. Create the outer message structure
        full_trans_msg = {
            "eventId": f"trans_evt_{i}_{time.time()}", # Example event ID
            "eventType": "TransactionCompleted",       # <<< SET THE CORRECT EVENT TYPE
            "eventTimestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "sourceSystem": "TestPublisher_Trans",     # Indicate source
            "version": "1.0",                         # Example version
            "payload": trans_payload                   # <<< NEST the payload
        }
        # 3. Publish the full message
        publish_message(channel, ACTIVITY_LOGS_QUEUE, full_trans_msg)

        # --- Auth log message ---
        # 1. Create the inner payload
        auth_payload = {
            "auth_attempt_id": f"uuid_{i}_{time.time()}",
            "client_id": random.randint(1, 2000),
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "authentication_method": random.choice(["Password-Email", "EID", "SMS", "MASIID"]),
            "status": "Success" if random.random() > 0.1 else "Failure",
            "source_ip": f"192.168.1.{random.randint(10, 200)}",
            "user_agent": "Test Publisher Script",
            "application_type": random.choice(["WebApp", "MobileApp"]),
            "failure_reason": None
        }
        if auth_payload["status"] == "Failure":
            auth_payload["failure_reason"] = "Invalid Credentials"

        # 2. Create the outer message structure (Matching the auth example)
        full_auth_msg = {
            "eventId": f"auth_evt_{i}_{time.time()}",     # Example event ID
            "eventType": "AuthenticationAttempt",      # <<< SET THE CORRECT EVENT TYPE
            "eventTimestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "sourceSystem": "TestPublisher_Auth",      # Indicate source
            "version": "1.0",                          # Example version
            "payload": auth_payload                    # <<< NEST the payload
        }
        # 3. Publish the full message
        publish_message(channel, AUTH_LOGS_QUEUE, full_auth_msg)

        time.sleep(0.5) # Simulate some delay

    connection.close()
    print("Publisher finished and connection closed.")

except pika.exceptions.AMQPConnectionError as e:
    print(f"Error connecting to RabbitMQ at {AMQP_HOST}:{AMQP_PORT} - {e}")
except Exception as e:
    print(f"An unexpected error occurred: {e}")