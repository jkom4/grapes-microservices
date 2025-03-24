## Auth service

### Run mongo sh command 
```bash
docker exec -it grapes-mongodb mongosh "mongodb://root:SparringMASI%21@localhost:27017/admin"
```

The main DB is `auth` and the main collection is `users`.

To use them is necessary to create a user with the following command:
```bash
use auth
db.find()
```

### To launch my service

```bash
docker compose up -d mongodb auth-service
``` 
