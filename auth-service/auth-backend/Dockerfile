FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
COPY libraries/pkcs11-provider-1.9.4.jar /tmp/pkcs11-provider-1.9.4.jar
COPY libraries/iaikPkcs11Provider.jar /tmp/iaikPkcs11Provider.jar
COPY libraries/sunpkcs11-wrapper-1.4.0.jar /tmp/sunpkcs11-wrapper-1.4.0.jar


RUN mvn install:install-file -Dfile=/tmp/pkcs11-provider-1.9.4.jar -DgroupId=iaik -DartifactId=pkcs11-provider -Dversion=1.9.4 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/tmp/iaikPkcs11Provider.jar -DgroupId=iaik -DartifactId=iaikPkcs11Provider -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/tmp/sunpkcs11-wrapper-1.4.0.jar -DgroupId=sun -DartifactId=sunpkcs11-wrapper -Dversion=1.4.0 -Dpackaging=jar

RUN mvn clean install -DskipTests


FROM eclipse-temurin:21-jdk-alpine
LABEL authors="jkom"


RUN apk add --no-cache opensc pcsc-lite pcsc-lite-dev eudev

# run pcscd
RUN mkdir -p /run/pcscd

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar


ENV AUTH_SERVICE_PORT=${AUTH_SERVICE_PORT}
ENV MONGO_URI=${AUTH_MONGO_URI}
ENV MONGO_DB=${AUTH_MONGO_DB}
ENV GRAPES_AES_KEY=${GRAPES_AES_KEY}
ENV GRAPES_SENDGRIP_API_SECRET=${GRAPES_SENDGRIP_API_SECRET}
ENV GRAPES_TWILIO_API_SECRET=${GRAPES_TWILIO_API_SECRET}
ENV GRAPES_TWILIO_SID=${GRAPES_TWILIO_SID}
ENV TWILIO_FROM_NUMBER=${TWILIO_FROM_NUMBER}
ENV COMPANY_MAIL=${COMPANY_MAIL}
ENV GRAPES_JWT_SECRET_KEY=${GRAPES_JWT_SECRET_KEY}
ENV ACCESS_TOKEN_EXPIRATION=${ACCESS_TOKEN_EXPIRATION}
ENV REFRESH_TOKEN_EXPIRATION=${REFRESH_TOKEN_EXPIRATION}
ENV MAX_REQUESTS_PER_MINUTE=${MAX_REQUESTS_PER_MINUTE}
ENV CHALLENGE_REQUEST_INTERVAL_IN_SECONDS=${CHALLENGE_REQUEST_INTERVAL_IN_SECONDS}
ENV CHALLENGE_VALIDITY_PERIOD_IN_SECONDS=${CHALLENGE_VALIDITY_PERIOD_IN_SECONDS}
ENV REQUIRED_AGE=${REQUIRED_AGE}

ENV AUTH_SERVICE_RABBIT_MQ_QUEUE_NAME=${AUTH_SERVICE_RABBIT_MQ_QUEUE_NAME}
ENV AUTH_SERVICE_RABBIT_MQ_QUEUE_REGISTRATION_NAME=${AUTH_SERVICE_RABBIT_MQ_QUEUE_REGISTRATION_NAME}
ENV RABBITMQ_HOST=${RABBITMQ_HOST}
ENV RABBITMQ_PORT=${RABBITMQ_PORT}
ENV RABBITMQ_USER=${RABBITMQ_USER}
ENV RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}

ENV REDIS_HOST=${REDIS_HOST}
ENV REDIS_PORT=${REDIS_PORT}
ENV REDIS_PASSWORD=${REDIS_PASSWORD}

EXPOSE ${AUTH_SERVICE_PORT}


ENTRYPOINT ["/bin/sh", "-c", "\
  pcscd && \
  java --add-exports=jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED \
       --enable-native-access=ALL-UNNAMED \
       -jar app.jar \
"]
