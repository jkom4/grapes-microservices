// models/User.js

/**
 * Model class for User
 */
/**
 * Model class for User
 */
export class User {
    /**
     * Create a new User
     * @param {string} login - Username or email
     * @param {string} password - Password
     * @param {string} phoneNumber - Phone number (optional)
     */
    constructor(login, password, phoneNumber = '') {
        this.login = login;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.authenticated = false;
    }

    /**
     * Get user login/username
     * @returns {string} Username
     */
    getLogin() {
        return this.login;
    }

    /**
     * Set user login/username
     * @param {string} login - Username
     */
    setLogin(login) {
        this.login = login;
    }

    /**
     * Get user password (for authentication purposes only)
     * @returns {string} Password
     */
    getPassword() {
        return this.password;
    }

    /**
     * Set user password
     * @param {string} password - Password
     */
    setPassword(password) {
        this.password = password;
    }

    /**
     * Check if user is authenticated
     * @returns {boolean} Authentication status
     */
    isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Set authentication status
     * @param {boolean} status - Authentication status
     */
    setAuthenticated(status) {
        this.authenticated = status;
    }

    /**
     * Get user email
     * @returns {string} Email address
     */
    getEmail() {
        return this.email;
    }

    /**
     * Set user email
     * @param {string} email - Email address
     */
    setEmail(email) {
        this.email = email;
    }

    /**
     * Get user phone number
     * @returns {string} Phone number
     */
    getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Set user phone number
     * @param {string} phoneNumber - Phone number
     */
    setPhoneNumber(phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}