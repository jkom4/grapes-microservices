FROM mariadb:latest
LABEL authors="jkom"
COPY ./config/my.cnf /etc/mysql/conf.d/
COPY ./config/init-db.sql /docker-entrypoint-initdb.d/
ENV MYSQL_ROOT_PASSWORD=SparringMASI! \
    MYSQL_USER=module \
    MYSQL_PASSWORD=SparringMASI!

EXPOSE 3306