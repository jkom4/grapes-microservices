# test_publisher.py
import pika
import json
import time
import os
import random

# Use the same connection details as the consumer might use
AMQP_HOST = os.getenv("RABBITMQ_HOST") # Connect to localhost since script runs on host
AMQP_PORT = int(os.getenv("RABBITMQ_PORT")) # Use the HOST port mapped in compose
AMQP_USER = os.getenv("RABBITMQ_USER") # Use user from .env
AMQP_PWD = os.getenv("RABBITMQ_PASSWORD") # Use password from .env
AMQP_VHOST = os.getenv("AMQP_VHOST", "/")

TRANSACTIONS_QUEUE = os.getenv("TRANSACTIONS_QUEUE", "q.transactions")
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
    channel.queue_declare(queue=TRANSACTIONS_QUEUE, durable=True)
    channel.queue_declare(queue=AUTH_LOGS_QUEUE, durable=True)

    # --- Send some test messages ---
    for i in range(5):
        # Transaction message
        trans_msg = {
            "client_id": random.randint(1, 2000),
            "product_id": random.randint(1, 50),
            "service_id": None,
            "quantity": random.randint(1, 5),
            "unit_price": round(random.uniform(1.0, 20.0), 2),
            "transaction_timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
            "payment_method": random.choice(["Visa", "Mastercard", "PayPal"]),
            "payment_status": "Success",
            "delivery_status": random.choice(["Shipped", "Processing", "Delivered"]),
            "delivery_time_days": random.randint(1, 7) if random.random() > 0.1 else None,
            "source_system": random.choice(["WebApp", "MobileApp"]),
            "total_amount": 0 # Calculate properly if needed, consumer might recalculate
        }
        trans_msg["total_amount"] = round(trans_msg["quantity"] * trans_msg["unit_price"], 2)
        publish_message(channel, TRANSACTIONS_QUEUE, trans_msg)

        # Auth log message
        auth_msg = {
            "auth_attempt_id": f"uuid_{i}_{time.time()}", # simple unique enough ID for test
            "client_id": random.randint(1, 2000),
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
            "authentication_method": random.choice(["Password-Email", "EID", "SMS", "MASIID"]),
            "status": "Success" if random.random() > 0.1 else "Failure",
            "source_ip": f"192.168.1.{random.randint(10, 200)}",
            "user_agent": "Test Publisher Script",
            "application_type": random.choice(["WebApp", "MobileApp"]),
            "failure_reason": None
        }
        if auth_msg["status"] == "Failure":
            auth_msg["failure_reason"] = "Invalid Credentials"
        publish_message(channel, AUTH_LOGS_QUEUE, auth_msg)

        time.sleep(0.5) # Simulate some delay

    connection.close()
    print("Publisher finished and connection closed.")

except pika.exceptions.AMQPConnectionError as e:
    print(f"Error connecting to RabbitMQ at {AMQP_HOST}:{AMQP_PORT} - {e}")
except Exception as e:
    print(f"An unexpected error occurred: {e}")