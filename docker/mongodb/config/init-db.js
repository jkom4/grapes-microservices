// Read credentials from environment variables for better security
const username = process.env.MONGO_INITDB_ROOT_USERNAME || "root";
const password = process.env.MONGO_INITDB_ROOT_PASSWORD || "SparringMASI!";

// Connect to the admin database
db = connect(`mongodb://${username}:${password}@grapes-mongodb-primary:27017/admin?authSource=admin`);

// Auth database
db = connect(`mongodb://${username}:${password}@grapes-mongodb-primary:27017/auth_db?authSource=admin`);
db.createCollection("placeholder");

// Chat database
db = connect(`mongodb://${username}:${password}@grapes-mongodb-primary:27017/chat_db?authSource=admin`);
db.createCollection("placeholder");
