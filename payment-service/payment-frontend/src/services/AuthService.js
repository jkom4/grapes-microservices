import { API_CONFIG } from './config';
//import { User } from '../models/User';

export class AuthService {
    // Static method to handle user authentication
    static async login(user) {
        try {
            // Send POST request to authentication endpoint
            const response = await fetch(`${API_CONFIG.AUTH_BASE_URL}/login`, {
                method: 'POST',
                mode: 'no-cors',  // Prevents CORS issues
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(user.toJSON())  // Convert user data to JSON string
            });

            // Parse and return response data
            return await response.json();
        } catch (error) {
            // Handle network or server errors
            throw new Error('Connection to server failed');
        }
    }
}