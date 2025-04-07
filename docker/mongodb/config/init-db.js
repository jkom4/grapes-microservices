db = connect("mongodb://root:SparringMASI!@grapes-mongodb-primary:27017/admin?authSource=admin");

//Auth db
db = connect("mongodb://root:SparringMASI!@grapes-mongodb-primary:27017/auth_db?authSource=admin");
db.createCollection("placeholder");

//Chat db
db = connect("mongodb://root:SparringMASI!@grapes-mongodb-primary:27017/chat_db?authSource=admin");
db.createCollection("placeholder");
