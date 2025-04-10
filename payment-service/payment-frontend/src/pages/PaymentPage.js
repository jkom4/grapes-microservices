import React, { useState } from 'react';
import '../global.css';
import SPMB from '../images/SMPB.png';
import { Card } from '../models/Card';
import { Payment } from '../models/Payment';
import { PaymentService } from '../services/PaymentService';

const PaymentPage = () => {
    // Form state for card details
    const [formData, setFormData] = useState({
        cardPart1: '',
        cardPart2: '',
        cardPart3: '',
        cardPart4: '',
        expiry: '',
        cvc: ''
    });
    const [errors, setErrors] = useState({});
    const [status, setStatus] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);

    const PAYMENT_AMOUNT = 5.39;

    // Handle input changes with validation
    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'expiry') {
            // Format expiry date as MM/YY
            const cleaned = value.replace(/\D/g, '');

            if (cleaned.length <= 4) {
                let formatted = cleaned;

                if (cleaned.length > 2) {
                    formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2);
                }

                // Validate month is between 1-12
                if (cleaned.length >= 2) {
                    const month = parseInt(cleaned.substring(0, 2));
                    if (month < 1 || month > 12) {
                        setErrors(prev => ({ ...prev, expiry: 'Month must be between 01-12' }));
                    } else {
                        setErrors(prev => {
                            const newErrors = { ...prev };
                            delete newErrors.expiry;
                            return newErrors;
                        });
                    }
                }

                setFormData(prev => ({ ...prev, [name]: formatted }));
            }
        } else if (name.startsWith('cardPart') && value.length <= 4 && /^\d*$/.test(value)) {
            // Handle card number parts (digits only)
            setFormData(prev => ({ ...prev, [name]: value }));
        } else if (name === 'cvc' && value.length <= 4 && /^\d*$/.test(value)) {
            // Handle CVC input (digits only)
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    // Validate all form fields before submission
    const validateForm = () => {
        let tempErrors = {};
        const card = new Card(
            formData.cardPart1,
            formData.cardPart2,
            formData.cardPart3,
            formData.cardPart4,
            formData.expiry,
            formData.cvc
        );
        const fullCardNumber = card.getFullCardNumber();

        // Validate card number format
        if (!fullCardNumber.match(/^\d{16}$/)) {
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }

        // Validate expiry date format and check if card is expired
        if (!formData.expiry.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
            tempErrors.expiry = 'Invalid expiry date (MM/YY)';
        } else {
            const [month, year] = formData.expiry.split('/');
            const expiryDate = new Date(2000 + parseInt(year), parseInt(month) - 1, 1);
            const today = new Date();

            if (expiryDate < today) {
                tempErrors.expiry = 'Card is expired';
            }
        }

        // Validate CVC format
        if (!formData.cvc.match(/^\d{3,4}$/)) {
            tempErrors.cvc = 'CVC must be 3 or 4 digits';
        }

        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    // Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            return;
        }

        setIsProcessing(true);
        setStatus('Verifying information...');

        try {
            // Create card and payment objects
            const card = new Card(
                formData.cardPart1,
                formData.cardPart2,
                formData.cardPart3,
                formData.cardPart4,
                formData.expiry,
                formData.cvc
            );

            const payment = new Payment(card, PAYMENT_AMOUNT);
            const success = await PaymentService.processPayment(payment);

            // Handle payment response
            if (success) {
                setStatus('Information verified. An OTP code has been sent. Validation process continues automatically...');
            } else {
                setStatus('Payment information verification failed');
            }
        } catch (error) {
            console.error('Error:', error);
            setStatus('An error occurred while processing your request');
        } finally {
            setIsProcessing(false);
        }
    };

    // Auto-focus next field when current is complete
    const handleCardInputKeyUp = (e, nextFieldName) => {
        if (e.target.value.length === 4 && nextFieldName) {
            document.querySelector(`input[name="${nextFieldName}"]`).focus();
        }
    };

    return (
        <div className="payment-container">
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="amount">{PAYMENT_AMOUNT} USD</div>
            <div className="merchant">Grapes</div>
            {status && <div className="status-message">{status}</div>}
            <form onSubmit={handleSubmit}>
                {/* Card number input fields (4 groups of 4 digits) */}
                <div className="input-container card-number-group">
                    <input
                        type="text"
                        name="cardPart1"
                        value={formData.cardPart1}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart2"
                        value={formData.cardPart2}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart3"
                        value={formData.cardPart3}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart4"
                        value={formData.cardPart4}
                        onChange={handleChange}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                </div>
                <span className="required">*</span>
                {errors.cardNumber && <span className="error">{errors.cardNumber}</span>}

                {/* Expiry date input (MM/YY) */}
                <div className="input-container">
                    <input
                        type="text"
                        name="expiry"
                        placeholder="MM/YY"
                        value={formData.expiry}
                        onChange={handleChange}
                        maxLength="5"
                        disabled={isProcessing}
                    />
                    <span className="required">*</span>
                    {errors.expiry && <span className="error">{errors.expiry}</span>}
                </div>

                {/* CVC/CVV input */}
                <div className="input-container">
                    <input
                        type="text"
                        name="cvc"
                        placeholder="CVC"
                        value={formData.cvc}
                        onChange={handleChange}
                        maxLength="4"
                        disabled={isProcessing}
                    />
                    <span className="required">*</span>
                    {errors.cvc && <span className="error">{errors.cvc}</span>}
                </div>

                {/* Submit button with dynamic text */}
                <button type="submit" disabled={isProcessing}>
                    {isProcessing ? 'Processing...' : `Pay ${PAYMENT_AMOUNT} USD`}
                </button>
            </form>
            {/* Payment method logos */}
            <div className="card-icons">
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/MasterCard_Logo.svg/200px-MasterCard_Logo.svg.png"
                    alt="Mastercard"
                />
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/200px-Visa_Inc._logo.svg.png"
                    alt="Visa"
                />
            </div>
        </div>
    );
};

export default PaymentPage;