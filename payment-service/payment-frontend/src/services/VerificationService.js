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
    // Verifies payment using OTP code and transactionId
    verifyPayment: async (verification) => { // verification contient { paymentToken: '...' }
        try {
            // Récupérer transactionId depuis sessionStorage
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('Verification service error: Missing pendingTransactionId in sessionStorage.');
                return {
                    success: false,
                    message: 'Verification context lost. Please restart the payment process.'
                };
            }

            console.log(`Sending completion request for transactionId: ${transactionId}`); // Log
            const response = await apiClient.post('/payment/complete', {
                paymentToken: verification.paymentToken,
                transactionId: Number(transactionId) // Envoyer l'ID avec le token, convertir en nombre
            });

            if (response.data.success) {
                // Nettoyer sessionStorage en cas de succès
                sessionStorage.removeItem('pendingTransactionId');
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
            const message = error.response?.data?.message || 'An error occurred during verification';
            // Si l'erreur est Conflict (409), le paiement a peut-être déjà été traité
            if (error.response?.status === 409) {
                // Tenter de nettoyer sessionStorage car la transaction est dans un état final
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + " (Already processed or expired)." };
            }
            return {
                success: false,
                message: message
            };
        }
    },

    // Fetches details of pending payment using transactionId
    getPendingPaymentDetails: async () => {
        try {
            // Récupérer transactionId depuis sessionStorage
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('getPendingPaymentDetails error: Missing pendingTransactionId in sessionStorage.');
                return {
                    success: false,
                    message: 'Payment context not found. Please initiate payment again.'
                };
            }

            // MODIFICATION: Passer transactionId en paramètre de requête
            const response = await apiClient.get(`/payment/pending-details?transactionId=${transactionId}`);

            if (response.data.success) {
                return {
                    success: true,
                    // Les détails sont directement sous response.data selon le code backend
                    details: {
                        merchantName: response.data.merchantName,
                        amount: response.data.amount,
                        cardNumber: response.data.maskedCardNumber
                    }
                };
            } else {
                // Si le backend renvoie GONE (410) ou NOT_FOUND (404), nettoyer sessionStorage
                if (response.status === 410 || response.status === 404) {
                    sessionStorage.removeItem('pendingTransactionId');
                }
                return {
                    success: false,
                    message: response.data.message || 'Could not retrieve details.'
                };
            }
        } catch (error) {
            console.error('Error fetching pending payment details:', error);
            const status = error.response?.status;
            // Si le backend renvoie GONE (410) ou NOT_FOUND (404) ou FORBIDDEN (403) ou UNAUTHORIZED(401)
            if (status === 410 || status === 404 || status === 403 || status === 401) {
                sessionStorage.removeItem('pendingTransactionId'); // Nettoyer contexte potentiellement invalide
            }
            return {
                success: false,
                message: error.response?.data?.message || 'Server error fetching payment details.'
            };
        }
    },
};