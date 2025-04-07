export class User {
    // Constructor with default empty credentials
    constructor(username = '', password = '') {
        this.username = username;
        this.password = password;
    }

    // Factory method to create from data object
    static parse(data) {
        return new User(data.username, data.password);
    }

    // Prepare user data for authentication
    toJSON() {
        return {
            username: this.username,
            password: this.password
        };
    }

    // String representation without password
    toString() {
        return `User: ${this.username}`;
    }
}