FROM mariadb:latest
LABEL authors="jkom"

COPY ./config/init-db.sql /docker-entrypoint-initdb.d/

ENV MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
ENV MYSQL_USER=${DB_USER}
ENV MYSQL_PASSWORD=${DB_PASSWORD}

EXPOSE ${DB_PORT}