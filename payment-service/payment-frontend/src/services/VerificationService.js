// src/services/VerificationService.js
import axios from 'axios';

// Base URL for API endpoints
const API_URL = 'http://localhost:8043/api';

// Axios instance with configuration
const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Important for maintaining session
});

export const VerificationService = {
    /**
     * Verify payment with OTP code
     * @param {PaymentVerification} verification - The verification details with OTP code
     * @returns {Promise} - Promise with verification result
     */
    verifyPayment: async (verification) => {
        try {
            const response = await apiClient.post('/payment/complete', verification.toJSON());

            if (response.data.success) {
                return {
                    success: true,
                    message: response.data.message,
                    transactionId: response.data.transactionId || 'Transaction completed'
                };
            } else {
                return {
                    success: false,
                    message: response.data.message || 'Verification failed'
                };
            }
        } catch (error) {
            console.error('Verification service error:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'An error occurred during verification'
            };
        }
    },

    /**
     * Get payment details based on payment ID
     * @param {string} paymentId - The payment ID
     * @returns {Promise} - Promise with payment details
     */
    getPaymentDetails: async (paymentId) => {
        try {
            // In a real system, this would fetch actual details from the server
            // For this example, we'll return mock data
            // You could implement an actual API call like:
            // const response = await apiClient.get(`/payment/details/${paymentId}`);
            // return response.data;

            // Mock data
            return {
                merchantName: 'Grapes',
                amount: 'EURO 45.99 €',
                cardNumber: 'XXXX XXXX XXXX 0237',
                dateTime: new Date().toLocaleString()
            };
        } catch (error) {
            console.error('Error fetching payment details:', error);
            return {
                merchantName: 'Grapes',
                amount: 'EURO 45.99 €',
                cardNumber: 'XXXX XXXX XXXX 0237'
            };
        }
    }
};