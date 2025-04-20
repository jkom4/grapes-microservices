// src/services/VerificationService.js
import axios from 'axios';
import { PAYMENT_API_URL } from './apiConfig'; // Assuming verification uses the same base URL as payment

// Axios instance configured for verification service endpoints
const apiClient = axios.create({
    baseURL: PAYMENT_API_URL, // Use the imported base URL
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Essential for session/auth context
});

// Handles payment verification (e.g., OTP submission) and related details retrieval
export const VerificationService = {
    /**
     * Verifies a payment using a verification token (e.g., OTP) and the pending transaction ID.
     * @param {PaymentVerification} verification - Object containing the paymentToken.
     * @returns {Promise<{success: boolean, message: string}>} - Result object.
     */
    verifyPayment: async (verification) => {
        try {
            // Retrieve the transaction ID stored during payment initiation
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('Verification service error: Missing pendingTransactionId in sessionStorage.');
                return {
                    success: false,
                    message: 'Verification context lost. Please restart the payment process.'
                };
            }

            console.log(`Sending completion request for transactionId: ${transactionId} with token: ${verification.paymentToken}`);
            // Endpoint path ('/payment/complete') is appended to the baseURL
            const response = await apiClient.post('/payment/complete', {
                paymentToken: verification.paymentToken,
                transactionId: Number(transactionId) // Ensure transactionId is sent as a number if required by backend
            });

            if (response.data && response.data.success) {
                // Clean up the transaction ID from session storage upon successful verification
                sessionStorage.removeItem('pendingTransactionId');
                console.log('Payment verification successful, pendingTransactionId removed.');
                return {
                    success: true,
                    message: response.data.message || 'Payment successful!'
                };
            } else {
                // Handle verification failure indicated by the backend
                return {
                    success: false,
                    message: response.data?.message || 'Verification failed. Please check the code and try again.'
                };
            }
        } catch (error) {
            console.error('Verification service error during completion:', error);
            const message = error.response?.data?.message || 'An error occurred during verification';
            const status = error.response?.status;

            // Handle specific errors like conflict (already processed) or not found/expired
            if (status === 409) { // Conflict - potentially already completed or failed
                sessionStorage.removeItem('pendingTransactionId'); // Clean up context
                return { success: false, message: message + " (Transaction may already be completed or expired)." };
            }
            if (status === 404 || status === 410) { // Not Found or Gone - transaction doesn't exist or expired
                sessionStorage.removeItem('pendingTransactionId'); // Clean up context
                return { success: false, message: message + " (Transaction not found or expired)." };
            }
            // General error message for other cases
            return {
                success: false,
                message: `Verification failed: ${message} ${status ? `(Status: ${status})` : '(Network error)'}`
            };
        }
    },

    /**
     * Fetches details (like merchant, amount, masked card number) for the pending payment verification.
     * @returns {Promise<{success: boolean, message?: string, details?: {merchantName: string, amount: string, cardNumber: string}}>} - Result object.
     */
    getPendingPaymentDetails: async () => {
        try {
            // Retrieve the transaction ID to identify which payment details to fetch
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('getPendingPaymentDetails error: Missing pendingTransactionId in sessionStorage.');
                return {
                    success: false,
                    message: 'Payment context not found. Please initiate payment again.'
                };
            }

            console.log(`Workspaceing pending payment details for transactionId: ${transactionId}`);
            // Endpoint path with transactionId as a query parameter
            const response = await apiClient.get(`/payment/pending-details?transactionId=${transactionId}`);

            if (response.data && response.data.success) {
                // Ensure the expected detail fields are present in the response
                const details = {
                    merchantName: response.data.merchantName || 'N/A',
                    amount: response.data.amount || 'N/A',
                    cardNumber: response.data.maskedCardNumber || 'XXXX XXXX XXXX XXXX'
                };
                return {
                    success: true,
                    details: details
                };
            } else {
                // Handle cases where details retrieval failed server-side
                const message = response.data?.message || 'Could not retrieve payment details.';
                // Clean up context if the transaction is explicitly gone or not found
                if (response.status === 410 || response.status === 404) {
                    sessionStorage.removeItem('pendingTransactionId');
                    return { success: false, message: message + " (Transaction not found or expired)." };
                }
                return {
                    success: false,
                    message: message
                };
            }
        } catch (error) {
            console.error('Error fetching pending payment details:', error);
            const status = error.response?.status;
            const message = error.response?.data?.message || 'Server error fetching payment details.';

            // Clean up context for specific error statuses indicating the transaction is invalid/inaccessible
            if (status === 401 || status === 403 || status === 404 || status === 410) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + ` (Status: ${status} - Session cleaned)` };
            }
            // General error
            return {
                success: false,
                message: message + (status ? ` (Status: ${status})` : ' (Network error)')
            };
        }
    },
};