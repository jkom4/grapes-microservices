db = connect("mongodb://localhost:27017/admin");

// Initialiser le ReplicaSet si ce n'est pas déjà fait
var status = rs.status();
if (status.ok === 0) {
    rs.initiate(
        {
            _id: "rs0",
            members: [
                { _id: 0, host: "grapes-mongodb-primary:27017" },
                { _id: 1, host: "grapes-mongodb-secondary:27017" },
                { _id: 2, host: "grapes-mongodb-arbiter:27017", arbiterOnly: true }
            ]
        }
    );
    print("✅ ReplicaSet initiated.");
} else {
    print("ℹ️ ReplicaSet already initiated.");
}

db = connect("mongodb://root:SparringMASI!@localhost:27017/auth_db?authSource=admin");
db.createCollection("placeholder");

db = connect("mongodb://root:SparringMASI!@localhost:27017/chat_db?authSource=admin");
db.createCollection("placeholder");
