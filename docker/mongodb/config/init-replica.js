rs.initiate({
    _id: "rs0",
    members: [
        { _id: 0, host: "grapes-mongodb-primary:27017" },
        { _id: 1, host: "grapes-mongodb-secondary:27017" },
        { _id: 2, host: "grapes-mongodb-arbiter:27017", arbiterOnly: true }
    ]
});
