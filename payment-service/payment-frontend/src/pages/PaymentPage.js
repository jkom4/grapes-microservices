// src/pages/PaymentPage.js
import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom'; // Used for declarative navigation
import SPMB from '../images/SMPB.png'; // Assuming this local bank logo exists
import { Card } from '../models/Card';
import { Payment } from '../models/Payment';
import { PaymentService } from '../services/PaymentService';
import { AuthService } from '../services/AuthService';

// Import local card logos - MAKE SURE YOU HAVE THESE FILES in src/images
// You might need to adjust filenames based on what you download
import mastercardLogo from '../images/MasterCardLogo.png';
import visaLogo from '../images/VisaInclogo.png';

/**
 * PaymentPage Component: Handles card input and initiates the payment process.
 */
const PaymentPage = () => {
    // State for form data
    const [formData, setFormData] = useState({
        cardPart1: '', cardPart2: '', cardPart3: '', cardPart4: '',
        expiry: '', cvc: ''
    });
    const [errors, setErrors] = useState({}); // Form validation errors
    const [status, setStatus] = useState(''); // User feedback message (processing, error)
    const [isProcessing, setIsProcessing] = useState(false); // Disables form during submission
    const [paymentState, setPaymentState] = useState('INPUT'); // Controls UI state: INPUT, PROCESSING, ERROR
    const [redirectToLogin, setRedirectToLogin] = useState(false); // Triggers navigation if not logged in
    const [redirectToVerification, setRedirectToVerification] = useState(false); // Triggers navigation on success


    // Effect Hook to check authentication status on component mount
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            console.log("User not logged in, redirecting to login.");
            setRedirectToLogin(true); // Set state to trigger Navigate component
        }
    }, []); // Empty dependency array ensures this runs only once on mount

    // Handles changes in form inputs, applying formatting where necessary
    const handleChange = (e) => {
        const { name, value } = e.target;

        // Reset general status message on input change
        if (status) setStatus('');
        // Reset specific field error on change
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: undefined }));
        }

        // Expiry date formatting (MM/YY) and basic validation
        if (name === 'expiry') {
            const cleaned = value.replace(/\D/g, ''); // Remove non-digits
            let formatted = cleaned;
            if (cleaned.length > 2) {
                // Insert slash after month
                formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2, 4);
            }
            // Basic month check (1-12)
            if (cleaned.length >= 2) {
                const month = parseInt(cleaned.substring(0, 2), 10);
                if (month < 1 || month > 12) {
                    setErrors(prev => ({ ...prev, expiry: 'Invalid month (01-12)' }));
                }
            }
            setFormData(prev => ({ ...prev, [name]: formatted }));
        }
        // Card number parts: allow only up to 4 digits
        else if (name.startsWith('cardPart') && /^\d{0,4}$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        }
        // CVC: allow only up to 4 digits
        else if (name === 'cvc' && /^\d{0,4}$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        }

    };

    // Validates the entire form before submission
    const validateForm = () => {
        let tempErrors = {};
        const currentYearLastTwoDigits = new Date().getFullYear() % 100;
        const currentMonth = new Date().getMonth() + 1; // JS months are 0-indexed

        // Card Number Validation (16 digits total)
        const fullCardNumber = formData.cardPart1 + formData.cardPart2 + formData.cardPart3 + formData.cardPart4;
        if (!/^\d{16}$/.test(fullCardNumber)) {
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }

        // Expiry Date Validation (MM/YY format and not expired)
        if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(formData.expiry)) {
            tempErrors.expiry = 'Expiry date must be in MM/YY format';
        } else {
            const [monthStr, yearStr] = formData.expiry.split('/');
            const month = parseInt(monthStr, 10);
            const yearLastTwoDigits = parseInt(yearStr, 10);

            if (yearLastTwoDigits < currentYearLastTwoDigits ||
                (yearLastTwoDigits === currentYearLastTwoDigits && month < currentMonth)) {
                tempErrors.expiry = 'Card has expired';
            }
        }

        // CVC Validation (3 or 4 digits)
        if (!/^\d{3,4}$/.test(formData.cvc)) {
            tempErrors.cvc = 'CVC must be 3 or 4 digits';
        }


        setErrors(tempErrors);
        // Form is valid if the tempErrors object has no keys
        return Object.keys(tempErrors).length === 0;
    };

    // Handles form submission: validates, calls payment service, handles response
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission behavior
        if (!validateForm()) {
            setStatus('Please correct the errors in the form.');
            setPaymentState('ERROR'); // Indicate error state for UI feedback
            return; // Stop submission if validation fails
        }

        setIsProcessing(true);
        setPaymentState('PROCESSING');
        setStatus('Processing payment initiation...');
        setErrors({}); // Clear previous errors

        try {
            const card = new Card(
                formData.cardPart1, formData.cardPart2, formData.cardPart3, formData.cardPart4,
                formData.expiry, formData.cvc
            );
            const payment = new Payment(card);

            const result = await PaymentService.processPayment(payment);

            if (result.success && result.transactionId) {
                setStatus('Payment initiated. Redirecting to verification...');
                // Set state to trigger navigation declaratively
                setRedirectToVerification(true);
            } else {
                // Handle failure from payment service
                setPaymentState('ERROR');
                setStatus(result.message || 'Payment initiation failed. Please try again.');
            }
        } catch (error) {
            // Handle unexpected errors during the API call
            console.error('Payment Page handleSubmit Error:', error);
            setPaymentState('ERROR');
            setStatus('An unexpected error occurred. Please try again later.');
        } finally {
            // Re-enable form controls regardless of outcome
            setIsProcessing(false);
        }
    };

    // Auto-focuses the next card input part when 4 digits are entered
    const handleCardInputKeyUp = (e, nextFieldName) => {
        if (e.target.value.length === 4 && nextFieldName) {
            // Find the next input field by name and focus it
            const nextInput = document.querySelector(`input[name="${nextFieldName}"]`);
            if (nextInput) {
                nextInput.focus();
            }
        }
    };

    // Conditional Rendering for Redirections
    if (redirectToLogin) {
        // Use Navigate component for client-side routing without full page reload
        return <Navigate to="/login" replace />; // 'replace' prevents adding this page to history
    }
    if (redirectToVerification) {
        // Redirect to the verification page upon successful payment initiation
        return <Navigate to="/verification" replace />;
    }

    // Render the Payment Form UI
    return (
        <div className="payment-container">
            {/* Header Section */}
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="merchant">Merchant: Grapes</div> {/* Consider making merchant name dynamic */}
            {/* Status/Error Message Display */}
            {status && (
                <div className={paymentState === 'ERROR' ? 'error-message' : 'status-message'}>
                    {status}
                </div>
            )}

            {/* Payment Form (conditionally rendered based on state) */}
            {/* Only show form in INPUT state or if retrying after error */}
            {(paymentState === 'INPUT' || paymentState === 'ERROR') && (
                <form onSubmit={handleSubmit} noValidate> {/* Add noValidate to rely on custom validation */}
                    {/* Card Number Input Group */}
                    <div className="input-container">
                        <label htmlFor="cardPart1">Card Number <span className="required">*</span></label>
                        <div className="card-number-group">
                            {/* Four input parts for the card number */}
                            <input id="cardPart1" type="text" inputMode="numeric" pattern="\d{4}" name="cardPart1" value={formData.cardPart1} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number first 4 digits"/>
                            <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart2" value={formData.cardPart2} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number second 4 digits"/>
                            <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart3" value={formData.cardPart3} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number third 4 digits"/>
                            <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart4" value={formData.cardPart4} onChange={handleChange} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number last 4 digits"/>
                        </div>
                        {errors.cardNumber && <span className="error">{errors.cardNumber}</span>}
                    </div>

                    {/* Expiry Date Input */}
                    <div className="input-container">
                        <label htmlFor="expiry">Expiration Date (MM/YY) <span className="required">*</span></label>
                        <input id="expiry" type="text" name="expiry" placeholder="MM/YY" value={formData.expiry} onChange={handleChange} maxLength="5" className={errors.expiry ? 'input-error' : ''} disabled={isProcessing} required />
                        {errors.expiry && <span className="error">{errors.expiry}</span>}
                    </div>

                    {/* CVC Input */}
                    <div className="input-container">
                        <label htmlFor="cvc">CVC <span className="required">*</span></label>
                        <input id="cvc" type="text" inputMode="numeric" pattern="\d{3,4}" name="cvc" placeholder="CVC" value={formData.cvc} onChange={handleChange} maxLength="4" className={errors.cvc ? 'input-error' : ''} disabled={isProcessing} required />
                        {errors.cvc && <span className="error">{errors.cvc}</span>}
                    </div>
                    {/* Submit Button */}
                    <button type="submit" disabled={isProcessing || paymentState === 'PROCESSING'}>
                        {isProcessing ? 'Processing...' : `Pay `}
                    </button>
                </form>
            )}

            {/* "Try Again" Button (Only shown on ERROR state after initial submission) */}
            {paymentState === 'ERROR' && !isProcessing && (
                <div>
                    <button onClick={() => {
                        // Reset state to allow user to retry input
                        setPaymentState('INPUT');
                        setStatus('');
                    }}>
                        Try Again
                    </button>
                </div>
            )}

            {/* Footer Section */}
            <div className="card-icons">
                {/* Use imported local images */}
                <img src={mastercardLogo} alt="Mastercard accepted" />
                <img src={visaLogo} alt="Visa accepted" />
            </div>
            <p className="security-note">Payment processed securely using 3D Secure protocol.</p>
        </div>
    );
};

export default PaymentPage;