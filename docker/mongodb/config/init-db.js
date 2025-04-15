db = connect("mongodb://localhost:27017/admin");

// Create the root user in the admin database
db.createUser({
    user: process.env.MONGO_USER_RW,
    pwd: process.env.MONGO_PASSWORD_RW,
    roles: [
        { role: "userAdminAnyDatabase", db: "admin" },
        { role: "readWriteAnyDatabase", db: "admin" }
    ]
});

// Connect to the auth_db database
db = connect(process.env.AUTH_MONGO_URI);
db.createCollection("placeholder"); // create a collection to initialize the database

// Connect to the chat_db database
db = connect(process.env.CHAT_MONGO_URI);
db.createCollection("placeholder");
