FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# Set environment variables
ENV GATEWAY_PORT=${GATEWAY_PORT}

EXPOSE ${GATEWAY_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
