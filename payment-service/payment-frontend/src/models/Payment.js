// models/Payment.js

/**
 * Model class for payment transactions
 */
export class Payment {
    /**
     * Create a new Payment
     * @param {Card} card - Payment card details
     * @param {number} amount - Payment amount
     * @param {string} currency - Currency code (default: USD)
     */
    constructor(card, amount, currency = 'USD') {
        this.id = this.generateId();
        this.card = card;
        this.amount = amount;
        this.currency = currency;
        this.status = 'INITIATED';
        this.timestamp = new Date();
        this.authCode = null;
    }

    /**
     * Generate a unique payment ID
     * @returns {string} Unique ID
     */
    generateId() {
        // Generate a simple UUID-like string
        return 'pmt_' + Math.random().toString(36).substring(2, 15) +
            Math.random().toString(36).substring(2, 15);
    }

    /**
     * Get payment ID
     * @returns {string} Payment ID
     */
    getId() {
        return this.id;
    }

    /**
     * Get payment card
     * @returns {Card} Card object
     */
    getCard() {
        return this.card;
    }

    /**
     * Get payment amount
     * @returns {number} Amount
     */
    getAmount() {
        return this.amount;
    }

    /**
     * Get formatted amount with currency
     * @returns {string} Formatted amount (e.g., "$10.99")
     */
    getFormattedAmount() {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: this.currency
        }).format(this.amount);
    }

    /**
     * Get payment currency
     * @returns {string} Currency code
     */
    getCurrency() {
        return this.currency;
    }

    /**
     * Get payment status
     * @returns {string} Status
     */
    getStatus() {
        return this.status;
    }

    /**
     * Set payment status
     * @param {string} status - New status
     */
    setStatus(status) {
        this.status = status;
    }

    /**
     * Get payment timestamp
     * @returns {Date} Timestamp
     */
    getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get formatted date/time
     * @returns {string} Formatted date and time
     */
    getFormattedDate() {
        return this.timestamp.toLocaleString();
    }

    /**
     * Get authentication code
     * @returns {string|null} Auth code
     */
    getAuthCode() {
        return this.authCode;
    }

    /**
     * Set authentication code
     * @param {string} authCode - Authentication code
     */
    setAuthCode(authCode) {
        this.authCode = authCode;
    }

    /**
     * Check if payment is completed
     * @returns {boolean} Completion status
     */
    isCompleted() {
        return this.status === 'COMPLETED';
    }

    /**
     * Check if payment is pending
     * @returns {boolean} Pending status
     */
    isPending() {
        return ['INITIATED', 'PROCESSING', 'VERIFIED'].includes(this.status);
    }

    /**
     * Check if payment failed
     * @returns {boolean} Failure status
     */
    isFailed() {
        return ['FAILED', 'REJECTED', 'EXPIRED'].includes(this.status);
    }
}