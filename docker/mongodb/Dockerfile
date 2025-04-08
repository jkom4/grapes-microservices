FROM mongo:latest
LABEL authors="kilane dounia"

# Copy the initialization scripts
COPY ./config/init-db.js /docker-entrypoint-initdb.d/
COPY ./config/init-replica.js /docker-entrypoint-initdb.d/

# Copy the security key
COPY ./mongo-keyfile /etc/mongo-keyfile

# Set very restrictive permissions
RUN chmod 400 /etc/mongo-keyfile

# Expose MongoDB port
EXPOSE 27017
