
// src/models/PaymentVerification.js
export class PaymentVerification {
    constructor(paymentToken, paymentId = null) {
        this.paymentToken = paymentToken;
        this.paymentId = paymentId;
    }

    // Convert to JSON for API requests
    toJSON() {
        return {
            paymentToken: this.paymentToken,
            paymentId: this.paymentId
        };
    }
}