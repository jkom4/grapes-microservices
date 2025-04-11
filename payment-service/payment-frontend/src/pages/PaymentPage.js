import React, { useState, useEffect } from 'react';
import '../global.css';
import SPMB from '../images/SMPB.png';
import { Card } from '../models/Card';
import { Payment } from '../models/Payment';
import { PaymentService } from '../services/PaymentService';
import { AuthService } from '../services/AuthService';

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
    const [paymentState, setPaymentState] = useState('INPUT'); // States: INPUT, PROCESSING, OTP, COMPLETED, ERROR
    const [otpCode, setOtpCode] = useState('');
    const [countdown, setCountdown] = useState(0);
    const [paymentId, setPaymentId] = useState('');

    const PAYMENT_AMOUNT = 5.39;

    // Check if user is authenticated
    useEffect(() => {
        if (!AuthService.isAuthenticated()) {
            window.location.href = '/login';
        }
    }, []);

    // Start countdown timer for OTP
    useEffect(() => {
        let timer;
        if (paymentState === 'OTP' && countdown > 0) {
            timer = setTimeout(() => {
                setCountdown(countdown - 1);
            }, 1000);
        } else if (countdown === 0 && paymentState === 'OTP') {
            setPaymentState('ERROR');
            setStatus('OTP verification timed out. Please try again.');
        }
        return () => clearTimeout(timer);
    }, [countdown, paymentState]);

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
        } else if (name === 'otpCode' && value.length <= 6 && /^\d*$/.test(value)) {
            // Handle OTP input
            setOtpCode(value);
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
        setPaymentState('PROCESSING');
        setStatus('Verifying card information...');

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
            const result = await PaymentService.processPayment(payment);

            // Handle payment response
            if (result.success) {
                setPaymentId(result.paymentId);
                setPaymentState('OTP');
                setStatus('Card verified. An OTP code has been sent to your registered mobile number. Please enter it below.');
                setCountdown(300); // 5 minutes countdown
            } else {
                setPaymentState('ERROR');
                setStatus(result.message || 'Payment verification failed');
            }
        } catch (error) {
            console.error('Error:', error);
            setPaymentState('ERROR');
            setStatus('An error occurred while processing your request. Please try again later.');
        } finally {
            setIsProcessing(false);
        }
    };

    // Handle OTP verification
    const handleOtpSubmit = async (e) => {
        e.preventDefault();

        if (otpCode.length !== 6) {
            setErrors(prev => ({ ...prev, otp: 'OTP code must be 6 digits' }));
            return;
        }

        setIsProcessing(true);
        setStatus('Verifying OTP code...');

        try {
            // Complete payment with OTP
            const result = await PaymentService.completePayment(paymentId, otpCode);

            if (result.success) {
                setPaymentState('COMPLETED');
                setStatus('Payment completed successfully! Transaction ID: ' + result.transactionId);
            } else {
                setErrors(prev => ({ ...prev, otp: result.message || 'Invalid OTP code' }));
            }
        } catch (error) {
            console.error('Error:', error);
            setErrors(prev => ({ ...prev, otp: 'An error occurred while verifying OTP' }));
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

    // Format time for countdown display
    const formatTime = (seconds) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    };

    return (
        <div className="payment-container">
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="amount">{PAYMENT_AMOUNT} USD</div>
            <div className="merchant">Grapes</div>
            {status && <div className="status-message">{status}</div>}

            {/* Card Input Form */}
            {paymentState === 'INPUT' && (
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
            )}

            {/* OTP Input Form */}
            {paymentState === 'OTP' && (
                <div className="otp-container">
                    <div className="otp-message">
                        <p>Enter the 6-digit OTP code sent to your registered mobile</p>
                        <p className="countdown">Time remaining: {formatTime(countdown)}</p>
                    </div>
                    <form onSubmit={handleOtpSubmit}>
                        <input
                            type="text"
                            name="otpCode"
                            placeholder="Enter OTP"
                            value={otpCode}
                            onChange={handleChange}
                            maxLength="6"
                            className="otp-input"
                            disabled={isProcessing}
                        />
                        {errors.otp && <span className="error">{errors.otp}</span>}
                        <button type="submit" disabled={isProcessing}>
                            {isProcessing ? 'Verifying...' : 'Verify OTP'}
                        </button>
                    </form>
                </div>
            )}

            {/* Payment Completed */}
            {paymentState === 'COMPLETED' && (
                <div className="payment-success">
                    <svg className="checkmark-circle" width="64" height="64" viewBox="0 0 52 52">
                        <circle className="checkmark-circle-bg" cx="26" cy="26" r="25" fill="none" />
                        <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                    </svg>
                    <h3>Payment Successful!</h3>
                    <p>Thank you for your payment.</p>
                    <button onClick={() => window.location.href = '/'}>Return to Homepage</button>
                </div>
            )}

            {/* Payment Failed */}
            {paymentState === 'ERROR' && (
                <div className="payment-error">
                    <svg className="error-circle" width="64" height="64" viewBox="0 0 52 52">
                        <circle className="error-circle-bg" cx="26" cy="26" r="25" fill="none" />
                        <path className="error-x" fill="none" d="M16 16 36 36 M36 16 16 36" />
                    </svg>
                    <h3>Payment Failed</h3>
                    <p>{status}</p>
                    <button onClick={() => {
                        setPaymentState('INPUT');
                        setStatus('');
                        setErrors({});
                    }}>Try Again</button>
                </div>
            )}

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