FROM openjdk:21-jdk-slim
LABEL authors="jkom"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "app.jar"]
