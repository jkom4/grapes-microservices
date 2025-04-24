// src/services/VerificationService.js
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
                console.error('Erreur: pendingTransactionId manquant dans sessionStorage.');
                return {
                    success: false,
                    message: 'Contexte de vérification perdu. Veuillez recommencer le processus de paiement.'
                };
            }

            console.log(`Envoi de la demande de finalisation pour l'ID: ${transactionId} avec le token: ${verification.paymentToken}`);
            const response = await apiClient.post('/payment/complete', {
                paymentToken: verification.paymentToken,
                transactionId: Number(transactionId)
            });

            if (response.data && response.data.success) {
                // Nettoyer l'ID de transaction en cas de succès
                sessionStorage.removeItem('pendingTransactionId');
                console.log('Vérification du paiement réussie, pendingTransactionId supprimé.');

                // Stocker l'URL de redirection si elle est présente dans la réponse
                if (response.data.redirectUrl) {
                    sessionStorage.setItem('redirectUrl', response.data.redirectUrl);
                    console.log(`URL de redirection stockée: ${response.data.redirectUrl}`);
                }

                return {
                    success: true,
                    message: response.data.message || 'Paiement réussi!',
                    redirectUrl: response.data.redirectUrl
                };
            } else {
                return {
                    success: false,
                    message: response.data?.message || 'Échec de la vérification. Veuillez vérifier le code et réessayer.'
                };
            }
        } catch (error) {
            console.error('Erreur pendant la vérification:', error);
            const message = error.response?.data?.message || 'Une erreur est survenue pendant la vérification';
            const status = error.response?.status;

            if (status === 409) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + " (La transaction peut déjà être terminée ou expirée)." };
            }
            if (status === 404 || status === 410) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + ` (Status: ${status})` };
            }

            if (status === 400 && message.includes("not found or expired")) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: "Requête de paiement introuvable ou expirée. Veuillez recommencer." };
            }

            return {
                success: false,
                message: `Échec de la vérification: ${message} ${status ? `(Status: ${status})` : '(Erreur réseau)'}`
            };
        }
    },

    getPendingPaymentDetails: async () => {
        try {
            const transactionId = sessionStorage.getItem('pendingTransactionId');
            if (!transactionId) {
                console.error('Erreur: pendingTransactionId manquant dans sessionStorage.');
                return {
                    success: false,
                    message: 'Contexte de paiement non trouvé. Veuillez initialiser le paiement à nouveau.'
                };
            }

            console.log(`Récupération des détails de paiement pour l'ID: ${transactionId}`);
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
                const message = response.data?.message || 'Impossible de récupérer les détails du paiement.';

                if (response.status === 410 || response.status === 404) {
                    sessionStorage.removeItem('pendingTransactionId');
                    return { success: false, message: message + ` (Status: ${response.status})` };
                }

                if (response.status === 400 && message.includes("not found or expired")) {
                    sessionStorage.removeItem('pendingTransactionId');
                    return { success: false, message: "Aucun paiement en attente trouvé ou il a expiré." };
                }

                return {
                    success: false,
                    message: message
                };
            }
        } catch (error) {
            console.error('Erreur lors de la récupération des détails de paiement:', error);
            const status = error.response?.status;
            const message = error.response?.data?.message || 'Erreur serveur lors de la récupération des détails.';

            if (status === 401 || status === 403 || status === 404 || status === 410) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: message + ` (Status: ${status} - Session nettoyée)` };
            }

            if (status === 400 && message.includes("not found or expired")) {
                sessionStorage.removeItem('pendingTransactionId');
                return { success: false, message: "Aucun paiement en attente trouvé ou il a expiré." };
            }

            return {
                success: false,
                message: message + (status ? ` (Status: ${status})` : ' (Erreur réseau)')
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