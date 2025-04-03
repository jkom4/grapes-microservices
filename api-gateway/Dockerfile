FROM openjdk:21-jdk-slim
LABEL authors="jkom"
WORKDIR /app
COPY target/*.jar app.jar
# Set environment variables
ENV GATEWAY_PORT=${GATEWAY_PORT}

EXPOSE ${GATEWAY_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
