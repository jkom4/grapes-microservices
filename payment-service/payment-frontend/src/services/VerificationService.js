// --- START OF src/services/VerificationService.js ---
import axios from 'axios';

// Base URL for API endpoints
const API_URL = 'http://localhost:8093/api';

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
            // Appel à /payment/complete avec le token OTP
            const response = await apiClient.post('/payment/complete', verification.toJSON());

            if (response.data.success) {
                return {
                    success: true,
                    message: response.data.message || 'Payment successful!', // Message générique
                    // transactionId retiré de la réponse backend
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
     * Get pending payment details from the backend session
     * @returns {Promise} - Promise with payment details (amount, maskedCardNumber, merchantName)
     */
    getPendingPaymentDetails: async () => {
        try {
            // Appel au nouvel endpoint GET
            const response = await apiClient.get('/payment/pending-details');
            if (response.data.success) {
                return {
                    success: true,
                    details: {
                        merchantName: response.data.merchantName,
                        amount: response.data.amount, // Le backend renvoie déjà formaté
                        cardNumber: response.data.maskedCardNumber // Le backend renvoie déjà masqué
                    }
                };
            } else {
                return { success: false, message: response.data.message || 'Could not retrieve details.' };
            }
        } catch (error) {
            console.error('Error fetching pending payment details:', error);
            return {
                success: false,
                // Tente de récupérer le message d'erreur du backend, sinon message générique
                message: error.response?.data?.message || 'Server error fetching payment details.'
            };
        }
    },
};
// --- END OF src/services/VerificationService.js ---