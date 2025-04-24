import axios from 'axios';
import { AUTH_API_URL } from './apiConfig'; // Import the specific URL

// Handles authentication-related operations
export class AuthService {
    // Authenticates user with email and password
    static async login(user) {
        try {
            // Use the imported URL and correct path
            console.log(`Sending authentication request to ${AUTH_API_URL}/login`);
            const response = await axios.post(`${AUTH_API_URL}/login`, {
                email: user.login, // Backend expects 'email'
                password: user.password
            }, {
                withCredentials: true // Important for session management
            });

            if (response.status === 200 && response.data && response.data.status === 'success') { // Check response data status
                console.log('Authentication successful:', response.data);
                // Assuming backend sends token (session ID) and userId upon successful login
                localStorage.setItem('sessionToken', response.data.token); // Store token if provided
                localStorage.setItem('userId', response.data.userId);     // Store userId if provided
                return {
                    success: true,
                    redirectUrl: '/payment', // Redirect to payment page after login
                    userId: response.data.userId
                };
            } else {
                // Handle cases where status is 200 but backend indicates failure
                return {
                    success: false,
                    error: response.data?.message || 'Authentication failed (server response)'
                };
            }
        } catch (error) {
            console.error('Login error:', error);
            if (error.response) {
                // Handle specific HTTP error statuses (401, 403, etc.)
                return {
                    success: false,
                    error: error.response.data?.message || `Authentication failed (status: ${error.response.status})`
                };
            } else if (error.request) {
                // Error: No response received from server
                return {
                    success: false,
                    error: 'No response from server. Please check network or server status.'
                };
            } else {
                // Other errors (e.g., setup issues)
                return {
                    success: false,
                    error: 'Login request failed. Please check your network connection.'
                };
            }
        }
    }

    // Checks if user is potentially authenticated (presence of token)
    static isLoggedIn() {
        // Basic check; real validation might involve checking token expiry or calling a backend endpoint
        return localStorage.getItem('sessionToken') !== null && localStorage.getItem('userId') !== null;
    }

    // Logs out user and clears session artifacts
    static logout() {
        localStorage.removeItem('sessionToken');
        localStorage.removeItem('userId');
        sessionStorage.removeItem('pendingTransactionId'); // Also clear pending transaction context
        window.location.href = '/login';
    }

    // Retrieves authenticated user's ID from local storage
    static getUserId() {
        return localStorage.getItem('userId');
    }

    // Retrieves session token from local storage
    static getToken() {
        return localStorage.getItem('sessionToken');
    }
}