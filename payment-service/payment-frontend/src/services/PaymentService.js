// src/services/PaymentService.js
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

export const PaymentService = {
    /**
     * Process a payment by initiating 3D Secure verification
     * @param {Payment} payment - The payment object with card details
     * @returns {Promise} - Promise with payment result
     */
    processPayment: async (payment) => {
        try {
            // Call the payment initiation endpoint
            const response = await apiClient.post('/payment/initiate', payment.toJSON());

            if (response.data.success) {
                // Store payment ID in session storage
                if (response.data.paymentId) {
                    sessionStorage.setItem('pendingPaymentId', response.data.paymentId);
                }
                return {
                    success: true,
                    message: response.data.message,
                    paymentId: response.data.paymentId
                };
            } else {
                return {
                    success: false,
                    message: response.data.message || 'Payment initiation failed'
                };
            }
        } catch (error) {
            console.error('Payment service error:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'An error occurred while processing your payment'
            };
        }
    },

    /**
     * Get current payment state from session
     * @returns {string|null} - Current payment ID or null
     */
    getPendingPaymentId: () => {
        return sessionStorage.getItem('pendingPaymentId');
    },

    /**
     * Clear pending payment from session
     */
    clearPendingPayment: () => {
        sessionStorage.removeItem('pendingPaymentId');
    }
};