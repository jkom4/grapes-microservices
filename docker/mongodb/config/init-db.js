db = connect("mongodb://localhost:27017/admin");

// Create the root user in the admin database
db.createUser({
    user: "root",
    pwd: "SparringMASI!",
    roles: [
        { role: "userAdminAnyDatabase", db: "admin" },
        { role: "readWriteAnyDatabase", db: "admin" }
    ]
});

// Connect to the auth_db database
db = connect("mongodb://root:SparringMASI!@localhost:27017/auth_db?authSource=admin");
db.createCollection("placeholder"); // create a collection to initialize the database

// Connect to the chat_db database
db = connect("mongodb://root:SparringMASI!@localhost:27017/chat_db?authSource=admin");
db.createCollection("placeholder");
