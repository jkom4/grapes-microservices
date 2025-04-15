FROM rabbitmq:3.8-management
LABEL authors="jkom"
RUN rabbitmq-plugins enable --offline rabbitmq_shovel rabbitmq_shovel_management
ENV RABBITMQ_DEFAULT_USER=${RABBITMQ_USER}
ENV RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}