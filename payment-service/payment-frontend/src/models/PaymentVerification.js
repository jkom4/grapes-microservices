export class PaymentVerification {
    // Constructor with default empty token
    constructor(paymentToken = '') {
        this.paymentToken = paymentToken;
    }

    // Factory method to create from data object
    static parse(data) {
        return new PaymentVerification(data.paymentToken);
    }

    // Serialize for API transmission
    toJSON() {
        return {
            paymentToken: this.paymentToken
        };
    }

    // String representation for logging
    toString() {
        return `Verification Token: ${this.paymentToken}`;
    }
}