// src/models/PaymentVerification.js
export class PaymentVerification {
    constructor(paymentToken) {
        this.paymentToken = paymentToken;
    }

    // Convert to JSON for API requests
    toJSON() {
        return {
            paymentToken: this.paymentToken
        };
    }
}