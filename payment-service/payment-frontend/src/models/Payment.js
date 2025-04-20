// src/models/Payment.js
export class Payment {
    constructor(card, amount = null) {
        this.cardNumber = card.getFullCardNumber();
        this.expirationDate = card.getFormattedExpiryDate(); // Uses the MM/YYYY format from Card model
        this.cvv = card.cvc;
        this.amount = amount;
        this.merchantName = null; // Will be set later if available
    }

    // Convert to JSON for API requests
    toJSON() {
        return {
            cardNumber: this.cardNumber,
            expirationDate: this.expirationDate,
            cvv: this.cvv,
            amount: this.amount,
            merchantName: this.merchantName
        };
    }
}