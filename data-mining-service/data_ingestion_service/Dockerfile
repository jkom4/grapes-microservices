# data_ingestion_service/Dockerfile

# Use an official Python runtime as a parent image
FROM python:3.9-slim

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE 1 # Prevents python from writing .pyc files
ENV PYTHONUNBUFFERED 1       # Force stdin, stdout, stderr to be totally unbuffered

# Set the working directory in the container
WORKDIR /app

# Install system dependencies that might be needed by some Python packages (e.g., mysql connector)
# Adjust if necessary based on specific connector requirements
RUN apt-get update && apt-get install -y --no-install-recommends \
    # Example: Add build-essential or default-libmysqlclient-dev if needed
    # Check mysql-connector-python documentation for prerequisites
    && rm -rf /var/lib/apt/lists/*
# Set timezone to UTC universally
ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the consumer script into the container
COPY consumer.py .

# Command to run the consumer script when the container launches
CMD ["python", "consumer.py"]