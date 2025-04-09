// First, import the Card class
import { Card } from './Card'; // Adjust the path based on your project structure

export class Payment {
    // Constructor for payment with card and amount
    constructor(card, amount) {
        this.card = card;
        this.amount = amount;
    }

    // Factory method to create Payment from data object
    static parse(data) {
        return new Payment(
            Card.parse(data.card),
            data.amount
        );
    }

    // Prepares payment data for API submission
    toJSON() {
        return {
            ...this.card.toJSON(),  // Spread card properties
            amount: this.amount      // Add payment amount
        };
    }

    // Human-readable representation
    toString() {
        return `Payment: ${this.amount} for ${this.card.toString()}`;
    }
}