// services/PaymentService.js
import axios from 'axios';
import { AuthService } from './AuthService';
import CryptoJS from 'crypto-js';

export class PaymentService {
    static PAYMENT_API_URL = 'https://localhost:8080/api/payment';
    static ENCRYPTION_KEY = AuthService.ENCRYPTION_KEY;

    // Utiliser la même méthode de chiffrement que AuthService
    static encrypt(data) {
        return CryptoJS.AES.encrypt(JSON.stringify(data), this.ENCRYPTION_KEY).toString();
    }

    static async processPayment(payment) {
        try {
            const token = AuthService.getToken();
            if (!token) {
                throw new Error('User not authenticated');
            }

            // Chiffrer les données de paiement
            const encryptedData = this.encrypt({
                cardNumber: payment.card.getFullCardNumber(),
                expiry: payment.card.getExpiry(),
                cvc: payment.card.getCvc(),
                amount: payment.amount,
                currency: 'USD'
            });

            // Envoyer les données chiffrées au serveur
            const response = await axios.post(`${this.PAYMENT_API_URL}/process`, {
                encryptedData: encryptedData
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data.paymentId) {
                localStorage.setItem('currentPaymentId', response.data.paymentId);
            }

            return response.data.success;
        } catch (error) {
            console.error('Payment error:', error);
            return false;
        }
    }

    // Autres méthodes comme avant...
}