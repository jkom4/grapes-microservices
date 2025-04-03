FROM openjdk:21-jdk-slim
LABEL authors="jkom"
WORKDIR /app
COPY target/*.jar app.jar


# Set environment variables
ENV PAYMENT_SERVICE_PORT=${PAYMENT_SERVICE_PORT}
ENV PAYMENT_DB_URL=${PAYMENT_DB_URL}
ENV PAYMENT_DB_USER=${PAYMENT_DB_USER}

EXPOSE ${PAYMENT_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
