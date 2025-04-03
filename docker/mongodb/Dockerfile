FROM mongo:latest
LABEL authors="jkom"

ENV MONGO_INITDB_ROOT_USERNAME=${MONGO_USER}
ENV MONGO_INITDB_ROOT_PASSWORD=${MONGO_PASSWORD}
COPY ./config/init-db.js /docker-entrypoint-initdb.d/
EXPOSE ${MONGO_PORT}