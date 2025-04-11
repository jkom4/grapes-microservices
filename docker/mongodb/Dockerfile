FROM mongo:latest
LABEL authors="jkom"

COPY ./config/init-db.js /docker-entrypoint-initdb.d/
EXPOSE ${MONGO_PORT}
