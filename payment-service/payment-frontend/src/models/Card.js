export class Card {
    constructor(cardPart1, cardPart2, cardPart3, cardPart4, expiryYYYY, cvc, cardholderName = '') {
        this.cardPart1 = cardPart1;
        this.cardPart2 = cardPart2;
        this.cardPart3 = cardPart3;
        this.cardPart4 = cardPart4;
        this.expiry = expiryYYYY; // Expecting MM/YYYY format passed in
        this.cvc = cvc;
        this.cardholderName = cardholderName;
    }

    // Get full card number by combining all parts
    getFullCardNumber() {
        return this.cardPart1 + this.cardPart2 + this.cardPart3 + this.cardPart4;
    }

    // Get formatted card number for display (with spaces)
    getFormattedCardNumber() {
        return `${this.cardPart1} ${this.cardPart2} ${this.cardPart3} ${this.cardPart4}`;
    }

    // Get expiration date in MM/YYYY format (property already holds this format)
    getFormattedExpiryDate() {
        return this.expiry;
    }

    // Get masked card number for display (only last 4 digits visible)
    getMaskedCardNumber() {
        return `XXXX XXXX XXXX ${this.cardPart4}`;
    }
}