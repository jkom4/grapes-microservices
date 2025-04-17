import axios from 'axios';

// Base configuration
const API_BASE_URL = 'http://localhost:8093';

// Handles authentication-related operations
export class AuthService {
    // Authenticates user with email and password
    static async login(user) {
        try {
            console.log(`Sending authentication request to ${API_BASE_URL}/api/login`);
            const response = await axios.post(`${API_BASE_URL}/api/login`, {
                email: user.login,
                password: user.password
            }, {
                withCredentials: true
            });
            if (response.status === 200) {
                console.log('Authentication successful:', response.data);
                localStorage.setItem('sessionToken', response.data.token);
                localStorage.setItem('userId', response.data.userId);
                return {
                    success: true,
                    redirectUrl: '/payment',
                    userId: response.data.userId
                };
            } else {
                return {
                    success: false,
                    error: response.data.message || 'Authentication failed'
                };
            }
        } catch (error) {
            console.error('Login error:', error);
            if (error.response) {
                return {
                    success: false,
                    error: error.response.data.message || 'Authentication failed'
                };
            } else if (error.request) {
                return {
                    success: false,
                    error: 'No response from server. Please try again later.'
                };
            } else {
                return {
                    success: false,
                    error: 'Connection error. Please check your network.'
                };
            }
        }
    }

    // Checks if user is authenticated
    static isLoggedIn() {
        return localStorage.getItem('sessionToken') !== null;
    }

    // Logs out user and clears session
    static logout() {
        localStorage.removeItem('sessionToken');
        localStorage.removeItem('userId');
        window.location.href = '/login';
    }

    // Retrieves authenticated user's ID
    static getUserId() {
        return localStorage.getItem('userId');
    }

    // Retrieves session token
    static getToken() {
        return localStorage.getItem('sessionToken');
    }
}