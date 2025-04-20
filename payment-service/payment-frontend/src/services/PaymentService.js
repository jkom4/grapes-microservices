// src/services/PaymentService.js
import axios from 'axios';
import { PAYMENT_API_URL } from './apiConfig'; // Import the specific URL

// Axios instance configured for payment service endpoints
const apiClient = axios.create({
    baseURL: PAYMENT_API_URL, // Use the imported base URL
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Crucial for maintaining session/authentication state with the backend
});

// Handles payment processing and transaction management
export const PaymentService = {
    /**
     * Initiates a 3D Secure payment process.
     * @param {Payment} payment - The payment object containing card details and amount.
     * @returns {Promise<{success: boolean, message: string, transactionId?: string}>} - Result object.
     */
    processPayment: async (payment) => {
        try {
            // The endpoint path ('/payment/initiate') is appended to the baseURL defined in apiClient
            const response = await apiClient.post('/payment/initiate', {
                cardNumber: payment.cardNumber,
                expirationDate: payment.expirationDate, // Should be MM/YYYY
                cvv: payment.cvv,
                amount: payment.amount, // Now included in the request
                merchantName: payment.merchantName // Include merchant name if available
            });

            if (response.data && response.data.success) {
                // Store the transaction ID received from the backend for the next step (verification)
                if (response.data.transactionId) {
                    sessionStorage.setItem('pendingTransactionId', response.data.transactionId);
                    console.log(`Stored pendingTransactionId: ${response.data.transactionId}`);
                } else {
                    console.warn('Payment initiation successful but no transactionId received.');
                }
                return {
                    success: true,
                    message: response.data.message || 'Payment initiated successfully.',
                    transactionId: response.data.transactionId
                };
            } else {
                // Handle cases where the request succeeded but the business logic failed
                return {
                    success: false,
                    message: response.data?.message || 'Payment initiation failed by server.'
                };
            }
        } catch (error) {
            console.error('Payment service error during initiation:', error);
            // Provide more specific error feedback based on the response
            const message = error.response?.data?.message ||
                (error.response ? `Server error (${error.response.status})` : 'Network or request error');
            return {
                success: false,
                message: `An error occurred while initiating your payment: ${message}`
            };
        }
    },

    /**
     * Retrieves the current pending transaction ID from session storage.
     * @returns {string | null} - The transaction ID or null if not found.
     */
    getPendingTransactionId: () => {
        return sessionStorage.getItem('pendingTransactionId');
    },

    /**
     * Clears the pending transaction ID from session storage.
     */
    clearPendingTransaction: () => {
        console.log('Clearing pendingTransactionId from sessionStorage.');
        sessionStorage.removeItem('pendingTransactionId');
    }
};