FROM openjdk:21-jdk-slim
LABEL authors="jkom"
WORKDIR /app
COPY target/api-gateway.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
