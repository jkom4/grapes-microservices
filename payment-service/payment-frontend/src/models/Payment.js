// src/models/Payment.js
export class Payment {
    constructor(card, amount, cardholderName = '') {
        this.cardNumber = card.getFullCardNumber();
        this.expirationDate = card.getFormattedExpiryDate();
        this.cvv = card.cvc;
        this.amount = amount;
        this.cardholderName = cardholderName || 'Card Holder';
    }

    // Convert to JSON for API requests
    toJSON() {
        return {
            cardNumber: this.cardNumber,
            expirationDate: this.expirationDate,
            cvv: this.cvv,
            cardholderName: this.cardholderName,
            amount: this.amount
        };
    }
}