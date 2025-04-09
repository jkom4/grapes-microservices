FROM maven:3.8.5-openjdk-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM openjdk:21-jdk-slim
LABEL authors="jkom"
WORKDIR /app
COPY --from=builder target/*.jar app.jar

# Set environment variables
ENV CHAT_SERVICE_PORT=${CHAT_SERVICE_PORT}
ENV CHAT_MONGO_URI=${CHAT_MONGO_URI}
ENV CHAT_MONGO_DB=${CHAT_MONGO_DB}

EXPOSE ${CHAT_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
