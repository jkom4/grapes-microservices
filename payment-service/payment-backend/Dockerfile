FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk-alpine
LABEL authors="jkom"
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar


# Set environment variables
ENV PAYMENT_SERVICE_PORT=${PAYMENT_SERVICE_PORT}
ENV PAYMENT_DB_URL=${PAYMENT_DB_URL}
ENV PAYMENT_DB_USER=${PAYMENT_DB_USER}

EXPOSE ${PAYMENT_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
