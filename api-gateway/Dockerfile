FROM maven:3.8.5-openjdk-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# Set environment variables
ENV GATEWAY_PORT=${GATEWAY_PORT}

EXPOSE ${GATEWAY_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
