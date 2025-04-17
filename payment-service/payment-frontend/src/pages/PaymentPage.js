import React, { useState, useEffect } from 'react';
import SPMB from '../images/SMPB.png';
import { Card } from '../models/Card';
import { Payment } from '../models/Payment';
import { PaymentService } from '../services/PaymentService';
import { AuthService } from '../services/AuthService';

// Manages payment form for card details and processes transactions
const PaymentPage = () => {
    // State for card form, validation errors, status, and processing
    const [formData, setFormData] = useState({
        cardPart1: '',
        cardPart2: '',
        cardPart3: '',
        cardPart4: '',
        expiry: '',
        cvc: ''
        // cardholderName: 'Card Holder' // RETIRÉ
    });
    const [errors, setErrors] = useState({});
    const [status, setStatus] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);
    const [paymentState, setPaymentState] = useState('INPUT'); // INPUT, PROCESSING, ERROR
    const PAYMENT_AMOUNT = 45.99;

    // Redirect to login if not authenticated
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
        }
    }, []);

    // Handle form input changes with formatting for expiry
    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name === 'expiry') {
            const cleaned = value.replace(/\D/g, '');
            let formatted = cleaned;
            if (cleaned.length > 2) {
                formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2,4); // Format as MM/YY
            }
            if (cleaned.length >= 2) {
                const month = parseInt(cleaned.substring(0, 2), 10);
                if (month < 1 || month > 12) {
                    setErrors(prev => ({ ...prev, expiry: 'Invalid month (01-12)' }));
                } else {
                    setErrors(prev => {
                        const newErrors = { ...prev };
                        delete newErrors.expiry;
                        return newErrors;
                    });
                }
            } else {
                setErrors(prev => {
                    const newErrors = { ...prev };
                    delete newErrors.expiry;
                    return newErrors;
                });
            }
            setFormData(prev => ({ ...prev, [name]: formatted }));
        } else if (name.startsWith('cardPart') && value.length <= 4 && /^\d*$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        } else if (name === 'cvc' && value.length <= 4 && /^\d*$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        }

    };

    // Validate form inputs before submission
    const validateForm = () => {
        let tempErrors = {};
        const card = new Card(
            formData.cardPart1, formData.cardPart2, formData.cardPart3, formData.cardPart4,
            formData.expiry, formData.cvc
            // formData.cardholderName // RETIRÉ
        );
        const fullCardNumber = card.getFullCardNumber();
        if (!fullCardNumber.match(/^\d{16}$/)) {
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }
        if (!formData.expiry.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
            tempErrors.expiry = 'Invalid expiry date format (MM/YY)';
        } else {
            const [monthStr, yearStr] = formData.expiry.split('/');
            const month = parseInt(monthStr, 10);
            const year = parseInt(`20${yearStr}`, 10);
            const now = new Date();
            const currentYear = now.getFullYear();
            const currentMonth = now.getMonth() + 1;
            if (year < currentYear || (year === currentYear && month < currentMonth)) {
                tempErrors.expiry = 'Card is expired';
            }
        }
        if (!formData.cvc.match(/^\d{3,4}$/)) {
            tempErrors.cvc = 'CVC must be 3 or 4 digits';
        }
        // if (!formData.cardholderName.trim()) {
        //     tempErrors.cardholderName = 'Cardholder name is required';
        // }
        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    // Handle payment submission and processing
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            return;
        }
        setIsProcessing(true);
        setPaymentState('PROCESSING');
        setStatus('Verifying card information...');
        try {
            const card = new Card(
                formData.cardPart1, formData.cardPart2, formData.cardPart3, formData.cardPart4,
                formData.expiry, formData.cvc
                // formData.cardholderName // RETIRÉ
            );
            const payment = new Payment(card, PAYMENT_AMOUNT);
            const result = await PaymentService.processPayment(payment);
            if (result.success) {
                // sessionStorage.setItem('pendingPaymentId', result.paymentId);
                window.location.href = `/verification`; // Redirect to OTP verification
            } else {
                setPaymentState('ERROR');
                setStatus(result.message || 'Payment verification failed');
            }
        } catch (error) {
            console.error('Error:', error);
            setPaymentState('ERROR');
            setStatus('An error occurred while processing your request.');
        } finally {
            setIsProcessing(false);
        }
    };

    // Auto-focus next card input field after 4 digits
    const handleCardInputKeyUp = (e, nextFieldName) => {
        if (e.target.value.length === 4 && nextFieldName) {
            document.querySelector(`input[name="${nextFieldName}"]`).focus();
        }
    };

    // Render payment form and UI
    return (
        <div className="payment-container">
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="merchant">Grapes</div>
            {status && <div className={paymentState === 'ERROR' ? 'error-message' : 'status-message'}>{status}</div>}
            {paymentState === 'INPUT' && (
                <form onSubmit={handleSubmit}>
                    <div className="input-container">
                        <label htmlFor="cardPart1">Card Number <span className="required">*</span></label>
                        <div className="card-number-group">
                            <input type="text" name="cardPart1" value={formData.cardPart1} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')} maxLength="4" placeholder="XXXX" className="card-part" disabled={isProcessing} required/>
                            <input type="text" name="cardPart2" value={formData.cardPart2} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')} maxLength="4" placeholder="XXXX" className="card-part" disabled={isProcessing} required/>
                            <input type="text" name="cardPart3" value={formData.cardPart3} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')} maxLength="4" placeholder="XXXX" className="card-part" disabled={isProcessing} required/>
                            <input type="text" name="cardPart4" value={formData.cardPart4} onChange={handleChange} maxLength="4" placeholder="XXXX" className="card-part" disabled={isProcessing} required/>
                        </div>
                        {errors.cardNumber && <span className="error">{errors.cardNumber}</span>}
                    </div>
                    <div className="input-container">
                        <label htmlFor="expiry">Expiration Date (MM/YY) <span className="required">*</span></label>
                        <input type="text" name="expiry" placeholder="MM/YY" value={formData.expiry} onChange={handleChange} maxLength="5" disabled={isProcessing} required />
                        {errors.expiry && <span className="error">{errors.expiry}</span>}
                    </div>
                    <div className="input-container">
                        <label htmlFor="cvc">CVC <span className="required">*</span></label>
                        <input type="text" name="cvc" placeholder="CVC" value={formData.cvc} onChange={handleChange} maxLength="4" disabled={isProcessing} required />
                        {errors.cvc && <span className="error">{errors.cvc}</span>}
                    </div>
                    <button type="submit" disabled={isProcessing}>
                        {isProcessing ? 'Processing...' : `Pay `}
                    </button>
                </form>
            )}
            {paymentState === 'ERROR' && (
                <div>
                    <button onClick={() => {
                        setPaymentState('INPUT');
                        setStatus('');
                        setErrors({});
                    }}>
                        Try Again
                    </button>
                </div>
            )}
            <div className="card-icons">
                <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/MasterCard_Logo.svg/200px-MasterCard_Logo.svg.png" alt="Mastercard" />
                <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/200px-Visa_Inc._logo.svg.png" alt="Visa" />
            </div>
            <p className="security-note">Secured by 3D Secure protocol</p>
        </div>
    );
};

export default PaymentPage;