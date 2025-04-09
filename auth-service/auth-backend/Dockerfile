FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests


FROM eclipse-temurin:21-jdk-alpine
LABEL authors="jkom"
WORKDIR /app
COPY --from=builder target/*.jar app.jar

# Set environment variables
ENV AUTH_SERVICE_PORT=${AUTH_SERVICE_PORT}
ENV MONGO_URI=${AUTH_MONGO_URI}
ENV MONGO_DB=${AUTH_MONGO_DB}
ENV GRAPES_AES_KEY=${GRAPES_AES_KEY}
ENV GRAPES_TWILIO_API_SECRET=${GRAPES_TWILIO_API_SECRET}
ENV COMPANY_MAIL=${COMPANY_MAIL}
ENV GRAPES_JWT_SECRET_KEY=${GRAPES_JWT_SECRET_KEY}

EXPOSE ${AUTH_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
