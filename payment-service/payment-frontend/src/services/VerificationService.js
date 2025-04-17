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

// Handles payment verification and retrieval of pending payment details
export const VerificationService = {
    // Verifies payment using OTP code
    verifyPayment: async (verification) => {
        try {
            const response = await apiClient.post('/payment/complete', {
                paymentToken: verification.paymentToken
            });
            if (response.data.success) {
                return {
                    success: true,
                    message: response.data.message || 'Payment successful!'
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

    // Fetches details of pending payment
    getPendingPaymentDetails: async () => {
        try {
            const response = await apiClient.get('/payment/pending-details');
            if (response.data.success) {
                return {
                    success: true,
                    details: {
                        merchantName: response.data.merchantName,
                        amount: response.data.amount,
                        cardNumber: response.data.maskedCardNumber
                    }
                };
            } else {
                return {
                    success: false,
                    message: response.data.message || 'Could not retrieve details.'
                };
            }
        } catch (error) {
            console.error('Error fetching pending payment details:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Server error fetching payment details.'
            };
        }
    },
};