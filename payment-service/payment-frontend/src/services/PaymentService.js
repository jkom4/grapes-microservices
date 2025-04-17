import axios from 'axios';

// Base URL for API endpoints
const API_URL = 'http://localhost:8093/api';

// Axios instance with default configuration
const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Enables session maintenance
});

// Handles payment processing and transaction management
export const PaymentService = {
    // Initiates 3D Secure payment with card details
    processPayment: async (payment) => {
        try {
            const response = await apiClient.post('/payment/initiate', {
                cardNumber: payment.cardNumber,
                expirationDate: payment.expirationDate,
                cvv: payment.cvv,
                amount: payment.amount,
                merchantName: "Grapes"
            });
            if (response.data.success) {
                if (response.data.transactionId) {
                    sessionStorage.setItem('pendingTransactionId', response.data.transactionId);
                }
                return {
                    success: true,
                    message: response.data.message,
                    transactionId: response.data.transactionId
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

    // Retrieves current transaction ID from session
    getPendingTransactionId: () => {
        return sessionStorage.getItem('pendingTransactionId');
    },

    // Clears pending transaction from session
    clearPendingTransaction: () => {
        sessionStorage.removeItem('pendingTransactionId');
    }
};