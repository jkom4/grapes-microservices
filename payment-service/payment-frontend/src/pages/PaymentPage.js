import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import axios from 'axios';
import SPMB from '../images/SMPB.png';
import { Card } from '../models/Card';
import { Payment } from '../models/Payment';
import { PaymentService } from '../services/PaymentService';
import { AuthService } from '../services/AuthService';
import { PAYMENT_API_URL } from '../services/apiConfig';


import mastercardLogo from '../images/MasterCardLogo.png';
import visaLogo from '../images/VisaInclogo.png';

/**
 * PaymentPage Component: Handles card input and initiates the payment process.
 */
const PaymentPage = () => {
    // State for form data
    const [formData, setFormData] = useState({
        cardPart1: '', cardPart2: '', cardPart3: '', cardPart4: '',
        expiry: '', // User inputs MM/YY
        cvc: ''
    });
    const [errors, setErrors] = useState({}); // Form validation errors
    const [status, setStatus] = useState(''); // User feedback message (processing, error)
    const [isProcessing, setIsProcessing] = useState(false); // Disables form during submission
    const [redirectToLogin, setRedirectToLogin] = useState(false); // Triggers navigation if not logged in
    const [redirectToVerification, setRedirectToVerification] = useState(false); // Triggers navigation on success
    const [paymentAmount, setPaymentAmount] = useState(null);
    const [merchantName, setMerchantName] = useState("Grapes");

    // Effect Hook to check authentication status and fetch payment details on component mount
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            console.log("User not logged in, redirecting to login.");
            setRedirectToLogin(true); // Set state to trigger Navigate component
            return;
        }

        // Fetch payment details from session
        const fetchPaymentDetails = async () => {
            try {
                const response = await axios.get(`${PAYMENT_API_URL}/payment/session-details`, {
                    withCredentials: true
                });

                if (response.data && response.data.success) {
                    setPaymentAmount(response.data.amount);
                    setMerchantName(response.data.merchantName || "Grapes");
                    console.log("Payment amount retrieved:", response.data.amount);
                } else {
                    setStatus('Could not retrieve payment details. Please try again.');
                }
            } catch (error) {
                console.error('Failed to fetch payment details:', error);
                setStatus('Could not retrieve payment amount. Please try again.');
            }
        };

        fetchPaymentDetails();
    }, []);

    // Handles changes in form inputs, applying formatting where necessary
    const handleChange = (e) => {
        const { name, value } = e.target;

        // Reset general status message on input change
        if (status) setStatus('');
        // Reset specific field error on change
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: undefined }));
        }

        // Expiry date formatting (MM/YY) - User Input Handling
        if (name === 'expiry') {
            const cleaned = value.replace(/\D/g, ''); // Remove non-digits
            let formatted = cleaned;
            // Keep length max 4 digits (MMYY) for input logic, format with slash
            if (cleaned.length > 2) {
                formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2, 4);
            } else {
                formatted = cleaned; // Allow MM or M
            }
            // Limit display length to 5 chars (MM/YY)
            if (formatted.length > 5) {
                formatted = formatted.substring(0, 5);
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

    // Validates the entire form before submission (using MM/YY format from input)
    const validateForm = () => {
        let tempErrors = {};
        const currentMonth = new Date().getMonth() + 1; // JS months are 0-indexed
        const currentFullYear = new Date().getFullYear(); // Use full year for comparison

        // Card Number Validation (16 digits total)
        const fullCardNumber = formData.cardPart1 + formData.cardPart2 + formData.cardPart3 + formData.cardPart4;
        if (!/^\d{16}$/.test(fullCardNumber)) { // Basic check for 16 digits, Luhn validation happens backend
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }

        // Expiry Date Validation (using MM/YY format from input)
        if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(formData.expiry)) {
            tempErrors.expiry = 'Expiry date must be in MM/YY format';
        } else {
            const [monthStr, yearStr] = formData.expiry.split('/');
            const month = parseInt(monthStr, 10);
            const yearLastTwoDigits = parseInt(yearStr, 10);

            // Convert YY to full year for comparison logic
            const inputFullYear = 2000 + yearLastTwoDigits;

            if (inputFullYear < currentFullYear || (inputFullYear === currentFullYear && month < currentMonth)) {
                tempErrors.expiry = 'Card has expired';
            }
            // Add check for unreasonably far future dates if needed
            if (inputFullYear > currentFullYear + 20) { // Example: Reject if expiry > 20 years in future
                tempErrors.expiry = 'Expiry date is too far in the future';
            }
        }

        // CVC Validation (3 or 4 digits)
        if (!/^\d{3,4}$/.test(formData.cvc)) {
            tempErrors.cvc = 'CVV must be 3 or 4 digits'; // Adjusted label to CVV
        }

        setErrors(tempErrors);
        // Form is valid if the tempErrors object has no keys
        return Object.keys(tempErrors).length === 0;
    };

    // Handles form submission: validates, converts date, calls payment service, handles response
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission behavior
        if (!validateForm()) {
            setStatus('Please correct the errors in the form.');
            return; // Stop submission if validation fails
        }

        if (!paymentAmount) {
            setStatus('Payment amount is missing. Please try again.');
            return;
        }

        setIsProcessing(true);
        setStatus('Processing payment initiation...');
        setErrors({}); // Clear previous errors

        try {
            // --- Convert MM/YY to MM/YYYY before sending ---
            const [month, yearShort] = formData.expiry.split('/');
            const fullYear = `20${yearShort}`; // Simple conversion assuming 20xx
            const expiryYYYY = `${month}/${fullYear}`;
            // ----------------------------------------------

            const card = new Card(
                formData.cardPart1, formData.cardPart2, formData.cardPart3, formData.cardPart4,
                expiryYYYY, // Pass the converted MM/YYYY date
                formData.cvc
            );

            // Create payment with amount
            const payment = new Payment(card, paymentAmount);
            payment.merchantName = merchantName;

            const result = await PaymentService.processPayment(payment);

            if (result.success && result.transactionId) {
                setStatus('Payment initiated. Redirecting to verification...');
                // Set state to trigger navigation declaratively
                setRedirectToVerification(true);
            } else {
                // Handle failure from payment service
                setStatus(result.message || 'Payment initiation failed. Please try again.');
            }
        } catch (error) {
            // Handle unexpected errors during the API call
            console.error('Payment Page handleSubmit Error:', error);
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

    // Format the amount for display if available
    const formattedAmount = paymentAmount ? `EUR ${parseFloat(paymentAmount).toFixed(2)}` : "Loading...";

    // Render the Payment Form UI
    return (
        <div className="payment-container">
            {/* Header Section */}
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="merchant">Merchant: {merchantName}</div>
            <div className="amount">{formattedAmount}</div>

            {/* Payment Form */}
            <form onSubmit={handleSubmit} noValidate> {/* Add noValidate to rely on custom validation */}
                {/* Card Number Input Group */}
                <div className="input-container">
                    <label htmlFor="cardPart1">Card Number <span className="required">*</span></label>
                    <div className="card-number-group">
                        {/* Four input parts for the card number */}
                        <input id="cardPart1" type="text" inputMode="numeric" pattern="\d{4}" name="cardPart1" value={formData.cardPart1} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number first 4 digits"/>
                        <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart2" value={formData.cardPart2} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number second 4 digits"/>
                        <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart3" value={formData.cardPart3} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number third 4 digits"/>
                        <input type="text" inputMode="numeric" pattern="\d{4}" name="cardPart4" value={formData.cardPart4} onChange={handleChange} onKeyUp={(e) => handleCardInputKeyUp(e, 'expiry')} maxLength="4" placeholder="XXXX" className={`card-part ${errors.cardNumber ? 'input-error' : ''}`} disabled={isProcessing} required aria-label="Card number last 4 digits"/>
                    </div>
                    {errors.cardNumber && <span className="error">{errors.cardNumber}</span>}
                </div>

                {/* Expiry Date Input */}
                <div className="input-container">
                    <label htmlFor="expiry">Expiration Date (MM/YY) <span className="required">*</span></label>
                    <input id="expiry" type="text" name="expiry" placeholder="MM/YY" value={formData.expiry} onChange={handleChange} maxLength="5" className={errors.expiry ? 'input-error' : ''} disabled={isProcessing} required />
                    {errors.expiry && <span className="error">{errors.expiry}</span>}
                </div>

                {/* CVV Input */}
                <div className="input-container">
                    <label htmlFor="cvc">CVV <span className="required">*</span></label> {/* Changed label */}
                    <input id="cvc" type="text" inputMode="numeric" pattern="\d{3,4}" name="cvc" placeholder="CVV" value={formData.cvc} onChange={handleChange} maxLength="4" className={errors.cvc ? 'input-error' : ''} disabled={isProcessing} required />
                    {errors.cvc && <span className="error">{errors.cvc}</span>}
                </div>
                {/* Submit Button */}
                <button type="submit" disabled={isProcessing}>
                    {isProcessing ? 'Processing...' : `Pay ${formattedAmount}`}
                </button>
            </form>
            {/* Status/Error Message Display */}
            {status && (
                // Display status/error messages directly
                <div className={Object.keys(errors).length > 0 || status.toLowerCase().includes('fail') || status.toLowerCase().includes('error') ? 'error-message' : 'status-message'}>
                    {status}
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