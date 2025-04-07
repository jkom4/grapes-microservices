FROM mongo:latest
LABEL authors="kilane dounia"

# Copie tes scripts init
COPY ./config/init-db.js /docker-entrypoint-initdb.d/
COPY ./config/init-replica.js /docker-entrypoint-initdb.d/

# Copie ta clé de sécurité
COPY ./mongo-keyfile /etc/mongo-keyfile

# Fixe les permissions TRES restrictives
RUN chmod 400 /etc/mongo-keyfile

# Expose port Mongo
EXPOSE 27017
