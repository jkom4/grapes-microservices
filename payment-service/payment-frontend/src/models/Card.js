// models/Card.js

/**
 * Model class for payment card details
 */
export class Card {
    /**
     * Create a new Card from individual card number parts
     * @param {string} part1 - First 4 digits of card
     * @param {string} part2 - Second 4 digits of card
     * @param {string} part3 - Third 4 digits of card
     * @param {string} part4 - Fourth 4 digits of card
     * @param {string} expiry - Expiration date in MM/YY format
     * @param {string} cvc - Card verification code
     */
    constructor(part1, part2, part3, part4, expiry, cvc) {
        if (part1 && part2 && part3 && part4) {
            this.cardNumber = part1 + part2 + part3 + part4;
        } else {
            this.cardNumber = '';
        }
        this.expiry = expiry;
        this.cvc = cvc;
    }

    /**
     * Get the full card number
     * @returns {string} Complete card number
     */
    getFullCardNumber() {
        return this.cardNumber;
    }

    /**
     * Get masked card number (only showing last 4 digits)
     * @returns {string} Masked card number
     */
    getMaskedCardNumber() {
        if (this.cardNumber && this.cardNumber.length >= 4) {
            return '************' + this.cardNumber.slice(-4);
        }
        return '****************';
    }

    /**
     * Get card expiry date
     * @returns {string} Expiration date (MM/YY)
     */
    getExpiry() {
        return this.expiry;
    }

    /**
     * Get card verification code
     * @returns {string} CVC/CVV code
     */
    getCvc() {
        return this.cvc;
    }

    /**
     * Check if card is valid (basic validation)
     * @returns {boolean} Validity status
     */
    isValid() {
        // Check if all fields are present
        if (!this.cardNumber || !this.expiry || !this.cvc) {
            return false;
        }

        // Check card number format (16 digits)
        if (!/^\d{16}$/.test(this.cardNumber)) {
            return false;
        }

        // Check expiry format (MM/YY)
        if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(this.expiry)) {
            return false;
        }

        // Check CVC format (3-4 digits)
        if (!/^\d{3,4}$/.test(this.cvc)) {
            return false;
        }

        // Check if card is expired
        const [month, year] = this.expiry.split('/');
        const expiryDate = new Date(2000 + parseInt(year), parseInt(month) - 1, 1);
        const today = new Date();

        if (expiryDate < today) {
            return false;
        }

        return true;
    }

    /**
     * Get the card's issuing network based on the first digits
     * @returns {string} Card network name (Visa, Mastercard, etc.)
     */
    getCardNetwork() {
        if (!this.cardNumber) {
            return 'Unknown';
        }

        // First digit is 4: Visa
        if (this.cardNumber.startsWith('4')) {
            return 'Visa';
        }

        // First digits 51-55: Mastercard
        if (/^5[1-5]/.test(this.cardNumber)) {
            return 'Mastercard';
        }

        // First digits 34 or 37: American Express
        if (/^3[47]/.test(this.cardNumber)) {
            return 'American Express';
        }

        // First digits 6011, 644-649, 65: Discover
        if (/^6(011|4[4-9]|5)/.test(this.cardNumber)) {
            return 'Discover';
        }

        return 'Unknown';
    }
}