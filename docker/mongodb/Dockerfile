FROM mongo:latest
LABEL authors="jkom"

ENV MONGO_INITDB_ROOT_USERNAME=root
ENV MONGO_INITDB_ROOT_PASSWORD=SparringMASI!
COPY ./config/init-db.js /docker-entrypoint-initdb.d/
EXPOSE 27017