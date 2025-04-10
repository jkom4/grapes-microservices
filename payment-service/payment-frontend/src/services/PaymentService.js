import { API_CONFIG } from './config';
//import { Payment } from '../models/Payment';

export class PaymentService {
    // Static method to handle payment processing
    static async processPayment(payment) {
        try {
            // Send payment data to server endpoint
            const response = await fetch(`${API_CONFIG.PAYMENT_BASE_URL}/process-payment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Basic ' + btoa('admin:admin123')  // Basic authentication
                },
                body: JSON.stringify(payment.toJSON())  // Serialize payment object
            });

            // Return success status based on response
            return response.ok;
        } catch (error) {
            console.error('Error:', error);
            // Propagate error with friendly message
            throw new Error('An error occurred while processing your request');
        }
    }
}