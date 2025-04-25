import axios from 'axios';
import { PAYMENT_API_URL } from './apiConfig';

const apiClient = axios.create({
    baseURL: PAYMENT_API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

export const VerificationService = {
    verifyPayment: async (verification) => {
        try {
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('Error: pendingTransactionId missing in sessionStorage.');
                return {
                    success: false,
                    message: 'Verification context lost. Please restart the payment process.'
                };
            }

            console.log(`Sending completion request for ID: ${transactionId} with token: ${verification.paymentToken}`);
            const response = await apiClient.post('/payment/complete', {
                paymentToken: verification.paymentToken,
                transactionId: Number(transactionId)
            });

            if (response.data && response.data.success) {
                // Clean transaction ID in case of success
                sessionStorage.removeItem('pendingTransactionId');
                console.log('Payment verification successful, pendingTransactionId removed.');

                // Store redirect URL if present in the response
                if (response.data.redirectUrl) {
                    sessionStorage.setItem('redirectUrl', response.data.redirectUrl);
                    console.log(`Redirect URL stored: ${response.data.redirectUrl}`);
                }

                return {
                    success: true,
                    message: response.data.message || 'Payment successful!',
                    redirectUrl: response.data.redirectUrl
                };
            } else {
                return {
                    success: false,
                    message: response.data?.message || 'Verification failed. Please check the code and try again.'
                };
            }
        } catch (error) {
            console.error('Error during verification:', error);
            const message = error.response?.data?.message || 'An error occurred during verification';
            const status = error.response?.status;

            if (status === 409) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + " (The transaction may already be completed or expired)." };
            }
            if (status === 404 || status === 410) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + ` (Status: ${status})` };
            }

            if (status === 400 && message.includes("not found or expired")) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: "Payment request not found or expired. Please try again." };
            }

            return {
                success: false,
                message: `Verification failed: ${message} ${status ? `(Status: ${status})` : '(Network error)'}`
            };
        }
    },

    getPendingPaymentDetails: async () => {
        try {
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('Error: pendingTransactionId missing in sessionStorage.');
                return {
                    success: false,
                    message: 'Payment context not found. Please initialize the payment again.'
                };
            }

            console.log(`Retrieving payment details for ID: ${transactionId}`);
            const response = await apiClient.get(`/payment/pending-details?transactionId=${transactionId}`);

            if (response.data && response.data.success) {
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
                const message = response.data?.message || 'Unable to retrieve payment details.';

                if (response.status === 410 || response.status === 404) {
                    sessionStorage.removeItem('pendingTransactionId');
                    return { success: false, message: message + ` (Status: ${response.status})` };
                }

                if (response.status === 400 && message.includes("not found or expired")) {
                    sessionStorage.removeItem('pendingTransactionId');
                    return { success: false, message: "No pending payment found or it has expired." };
                }

                return {
                    success: false,
                    message: message
                };
            }
        } catch (error) {
            console.error('Error retrieving payment details:', error);
            const status = error.response?.status;
            const message = error.response?.data?.message || 'Server error while retrieving details.';

            if (status === 401 || status === 403 || status === 404 || status === 410) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + ` (Status: ${status} - Session cleaned)` };
            }

            if (status === 400 && message.includes("not found or expired")) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: "No pending payment found or it has expired." };
            }

            return {
                success: false,
                message: message + (status ? ` (Status: ${status})` : ' (Network error)')
            };
        }
    },

    getRedirectUrl: () => {
        return sessionStorage.getItem('redirectUrl');
    },

    clearRedirectUrl: () => {
        sessionStorage.removeItem('redirectUrl');
    }
};