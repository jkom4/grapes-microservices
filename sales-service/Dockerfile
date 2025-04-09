FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk-alpine
LABEL authors="jkom"
WORKDIR /app
COPY --from=builder target/*.jar app.jar


# Set environment variables
ENV SALES_SERVICE_PORT=${SALES_SERVICE_PORT}
ENV SALES_DB_URL=${SALES_DB_URL}
ENV SALES_DB_USER=${SALES_DB_USER}
ENV SALES_DB_PASSWORD=${SALES_DB_PASSWORD}


EXPOSE ${SALES_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]