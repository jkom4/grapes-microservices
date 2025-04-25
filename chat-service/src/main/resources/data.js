db.chats.insertMany([
    {
        _id: "1",
        topic: "General"
    },
    {
        _id: "2",
        topic: "Support"
    },
    {
        _id: "3",
        topic: "Suggestions"
    },
    {
        _id: "4",
        topic: "Promotions and Offers"
    },
    {
        _id: "5",
        topic: "Private Discussions"
    },
    {
        _id: "6",
        topic: "Customer Feedback"
    },
    {
        _id: "7",
        topic: "Events and Announcements"
    },
    {
        _id: "8",
        topic: "Products and Inventory"
    },
    {
        _id: "9",
        topic: "Community and Partnerships"
    },
    {
        _id: "10",
        topic: "Frequently Asked Questions"
    }
]);

db.messages.insertMany([
    {
        _id: "1",
        senderId: "1",
        username: "Nas",
        content: "Hello, everyone!",
        createdAt: "2025-04-24T12:00:00.000Z",
        chatId: "1"
    },
    {
        _id: "2",
        senderId: "Alex",
        username: "2",
        content: "Welcome to the group!",
        createdAt: "2025-04-24T12:01:00.000Z",
        chatId: "1"
    }
]);