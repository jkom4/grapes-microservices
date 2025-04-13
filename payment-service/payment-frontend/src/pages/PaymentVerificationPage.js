import React, { useState, useEffect } from 'react';
import MASIB from '../images/MASIB.png';
import VISA from '../images/verified_by_visa.png';
import MASTC from '../images/mastercardsecurecode_logo.png';
import { PaymentVerification } from '../models/PaymentVerification';
import { VerificationService } from '../services/VerificationService';
import { AuthService } from '../services/AuthService';

const PaymentVerificationPage = () => {
    // State for OTP input and status messages
    const [paymentToken, setPaymentToken] = useState('');
    const [showError, setShowError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState(null);
    const [paymentId, setPaymentId] = useState('');
    const [paymentDetails, setPaymentDetails] = useState({
        merchantName: 'Grapes',
        amount: 'EURO 45.99 €',
        cardNumber: 'XXXX XXXX XXXX 0237'
    });
    const [isLoading, setIsLoading] = useState(false);
    const [remainingTime, setRemainingTime] = useState(180); // 3 minutes in seconds

    // Check if user is authenticated
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }

        // Get payment ID from URL
        const urlParams = new URLSearchParams(window.location.search);
        const paymentIdParam = urlParams.get('paymentId');

        if (paymentIdParam) {
            setPaymentId(paymentIdParam);
            // Fetch payment details using the payment ID
            fetchPaymentDetails(paymentIdParam);
        } else {
            // Try to get payment ID from session storage
            const storedPaymentId = sessionStorage.getItem('pendingPaymentId');
            if (storedPaymentId) {
                setPaymentId(storedPaymentId);
                fetchPaymentDetails(storedPaymentId);
            } else {
                // No payment in progress, redirect to payment page
                window.location.href = '/payment';
            }
        }

        // Start countdown timer
        const timer = setInterval(() => {
            setRemainingTime(prevTime => {
                if (prevTime <= 1) {
                    clearInterval(timer);
                    setShowError(true);
                    setErrorMessage('Verification time expired. Please try again.');
                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        // Clean up timer on unmount
        return () => clearInterval(timer);
    }, []);

    // Format remaining time as MM:SS
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
    };

    // Fetch payment details from the server
    const fetchPaymentDetails = async (id) => {
        try {
            // Get payment details based on the payment ID
            const details = await VerificationService.getPaymentDetails(id);
            setPaymentDetails({
                merchantName: details.merchantName || 'Grapes',
                amount: details.amount || 'EURO 45.99 €',
                cardNumber: details.cardNumber || 'XXXX XXXX XXXX 0237'
            });
        } catch (error) {
            console.error('Error fetching payment details:', error);
            // Set default values if fetching fails
        }
    };

    // Handle verification form submission
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic validation
        if (paymentToken.trim() === "") {
            setShowError(true);
            setErrorMessage("Please enter the verification code.");
            return;
        }

        setIsLoading(true);
        setShowError(false);

        try {
            // Create verification object and submit to service
            const verification = new PaymentVerification(paymentToken, paymentId);
            const result = await VerificationService.verifyPayment(verification);

            if (result.success) {
                // Handle successful verification
                setShowError(false);
                setSuccessMessage(result.transactionId || 'Transaction completed successfully');

                // Clear pending payment ID from session storage
                sessionStorage.removeItem('pendingPaymentId');

                // Redirect after a delay
                setTimeout(() => {
                    window.location.href = '/payment-success';
                }, 3000);
            } else {
                // Show error on failure
                setShowError(true);
                setErrorMessage(result.message || 'Verification failed. Please check the code and try again.');
            }
        } catch (error) {
            console.error('Error submitting payment:', error);
            setShowError(true);
            setErrorMessage('An error occurred during verification. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="verification-container">
            <div className="verification-box">
                <div className="logo-container">
                    <div className="left-logo">
                        <img src={MASIB} alt="Bank Logo" />
                    </div>
                    <div className="right-logos">
                        <img src={VISA} alt="Verified by Visa" />
                        <img src={MASTC} alt="MasterCard SecureCode" />
                    </div>
                </div>

                <p>A verification code has been sent to your registered phone number.</p>
                <p>Time remaining: {formatTime(remainingTime)}</p>

                <form onSubmit={handleSubmit}>
                    {/* Transaction details display (non-editable) */}
                    <div className="form-group">
                        <label>Merchant name:</label>
                        <input type="text" value={paymentDetails.merchantName} disabled />
                    </div>

                    <div className="form-group">
                        <label>Amount of the transaction:</label>
                        <input type="text" value={paymentDetails.amount} disabled />
                    </div>

                    <div className="form-group">
                        <label>Card number:</label>
                        <input type="text" value={paymentDetails.cardNumber} disabled />
                    </div>

                    {/* OTP input field */}
                    <div className="form-group">
                        <label>Enter the verification code:</label>
                        <input
                            type="text"
                            placeholder="Enter the verification code here"
                            value={paymentToken}
                            onChange={(e) => setPaymentToken(e.target.value.replace(/\D/g, '').substring(0, 6))}
                            disabled={isLoading || remainingTime === 0 || successMessage !== null}
                            maxLength={6}
                            autoFocus
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || remainingTime === 0 || successMessage !== null}
                    >
                        {isLoading ? 'Verifying...' : 'Confirm Payment'}
                    </button>
                </form>

                {/* Error message display */}
                {showError && (
                    <p className="error-message">
                        {errorMessage}
                    </p>
                )}

                {/* Success message with redirect notification */}
                {successMessage && (
                    <div className="success-message">
                        <p>
                            Payment successful! Transaction ID: <strong>{successMessage}</strong>
                        </p>
                        <p>
                            Redirecting to confirmation page in 3 seconds...
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentVerificationPage;