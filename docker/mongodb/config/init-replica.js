db = connect("mongodb://root:SparringMASI!@grapes-mongodb-primary:27017/admin?authSource=admin");

try {
    var status = rs.status();
    if (status.ok === 0) {
        print("Initiating ReplicaSet...");
        rs.initiate({
            _id: "rs0",
            members: [
                { _id: 0, host: "grapes-mongodb-primary:27017" },
                { _id: 1, host: "grapes-mongodb-secondary:27017" },
                { _id: 2, host: "grapes-mongodb-arbiter:27017", arbiterOnly: true }
            ]
        });
        print("ReplicaSet initiated.");
    } else {
        print("ReplicaSet already initiated.");
    }
} catch (e) {
    print("Waiting for Mongo to be ready, retrying in a few seconds...");
    sleep(5000); //wait 5 secondes
    rs.initiate({
        _id: "rs0",
        members: [
            { _id: 0, host: "grapes-mongodb-primary:27017" },
            { _id: 1, host: "grapes-mongodb-secondary:27017" },
            { _id: 2, host: "grapes-mongodb-arbiter:27017", arbiterOnly: true }
        ]
    });
    print("ReplicaSet initiated after retry.");
}
