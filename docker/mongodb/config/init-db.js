db = connect("mongodb://localhost:27017/admin");
db.createUser({
    user: "myuser",
    pwd: "SparringMASI!",
    roles: [{ role: "readWrite", db: "admin" }]
});
databases = ["db_auth"];

databases.forEach(dbName => {
    db.getSiblingDB(dbName).createCollection("init");
    print("Database created: " + dbName);
});
