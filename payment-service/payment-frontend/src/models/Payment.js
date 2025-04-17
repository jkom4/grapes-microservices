// src/models/Payment.js
export class Payment {
    constructor(card, amount) {
        this.cardNumber = card.getFullCardNumber();
        this.expirationDate = card.getFormattedExpiryDate();
        this.cvv = card.cvc;
        this.amount = amount;
    }

    // Convert to JSON for API requests
    toJSON() {
        return {
            cardNumber: this.cardNumber,
            expirationDate: this.expirationDate,
            cvv: this.cvv,
            amount: this.amount
        };
    }
}