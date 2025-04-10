export class Card {
    // Constructor with default values
    constructor(cardPart1 = '', cardPart2 = '', cardPart3 = '', cardPart4 = '', expiry = '', cvc = '') {
        this.cardPart1 = cardPart1;
        this.cardPart2 = cardPart2;
        this.cardPart3 = cardPart3;
        this.cardPart4 = cardPart4;
        this.expiry = expiry;
        this.cvc = cvc;
    }

    // Factory method to create a Card from data object
    static parse(data) {
        return new Card(
            data.cardPart1,
            data.cardPart2,
            data.cardPart3,
            data.cardPart4,
            data.expiry,
            data.cvc
        );
    }

    // Combines all parts into complete card number
    getFullCardNumber() {
        return `${this.cardPart1}${this.cardPart2}${this.cardPart3}${this.cardPart4}`;
    }

    // Returns PCI-compliant masked card number
    getMaskedCardNumber() {
        return `XXXX XXXX XXXX ${this.cardPart4}`;
    }

    // Prepares data for API transmission
    toJSON() {
        return {
            cardNumber: this.getFullCardNumber(),
            expiry: this.expiry,
            cvc: this.cvc
        };
    }

    // String representation for display/logging
    toString() {
        return `Card: ${this.getMaskedCardNumber()}, Expiry: ${this.expiry}`;
    }
}