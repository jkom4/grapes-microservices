FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk-alpine
LABEL authors="jkom"
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Set environment variables
ENV CHAT_SERVICE_PORT=${CHAT_SERVICE_PORT}
ENV CHAT_MONGO_URI=${CHAT_MONGO_URI}
ENV CHAT_MONGO_DB=${CHAT_MONGO_DB}

EXPOSE ${CHAT_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
