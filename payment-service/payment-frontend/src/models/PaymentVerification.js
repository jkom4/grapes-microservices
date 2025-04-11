// src/models/PaymentVerification.js

export class PaymentVerification {
    /**
     * Constructeur de la classe PaymentVerification
     * @param {string} paymentId - Identifiant du paiement
     * @param {string} verificationCode - Code de vérification
     * @param {number} amount - Montant du paiement (optionnel)
     */
    constructor(paymentId, verificationCode, amount = null) {
        this.paymentId = paymentId;
        this.verificationCode = verificationCode;
        this.amount = amount;
    }
}

export default PaymentVerification;