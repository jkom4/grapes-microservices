import React, { useState, useEffect } from 'react';
import MASIB from '../images/MASIB.png';
import VISA from '../images/verified_by_visa.png';
import MASTC from '../images/mastercardsecurecode_logo.png';
import { PaymentVerification } from '../models/PaymentVerification';
import { VerificationService } from '../services/VerificationService';
import { AuthService } from '../services/AuthService';

// Handles OTP verification for payment confirmation
const PaymentVerificationPage = () => {
    // State for OTP input, errors, success, payment details, loading, and timer
    const [paymentToken, setPaymentToken] = useState('');
    const [showError, setShowError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState(null);
    const [paymentDetails, setPaymentDetails] = useState({
        merchantName: 'Loading...',
        amount: 'Loading...',
        cardNumber: 'Loading...'
    });
    const [isLoading, setIsLoading] = useState(false);
    const [detailsLoading, setDetailsLoading] = useState(true);
    const [remainingTime, setRemainingTime] = useState(180);
    const [detailsFetched, setDetailsFetched] = useState(false);

    // Fetch payment details and manage timer
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }
        let timerId = null;
        const fetchDetails = async () => {
            if (detailsFetched && !errorMessage) return;
            setDetailsLoading(true);
            setErrorMessage('');
            try {
                const result = await VerificationService.getPendingPaymentDetails();
                if (result.success) {
                    setPaymentDetails(result.details);
                    setErrorMessage('');
                    setDetailsFetched(true);
                } else {
                    setErrorMessage(result.message || "Could not load payment details.");
                    setPaymentDetails({ merchantName: 'N/A', amount: 'N/A', cardNumber: 'N/A' });
                }
            } catch (error) {
                setErrorMessage("Failed to fetch payment details due to a network or server error.");
                setPaymentDetails({ merchantName: 'Error', amount: 'Error', cardNumber: 'Error' });
            } finally {
                setDetailsLoading(false);
            }
        };
        if (!detailsFetched) {
            fetchDetails();
        }
        if (!detailsLoading && !errorMessage && !successMessage && remainingTime > 0) {
            timerId = setInterval(() => {
                setRemainingTime(prevTime => {
                    if (prevTime <= 1) {
                        clearInterval(timerId);
                        if (!successMessage) {
                            setShowError(true);
                            setErrorMessage('Verification time expired. Please restart the payment.');
                        }
                        return 0;
                    }
                    return prevTime - 1;
                });
            }, 1000);
        }
        return () => {
            if (timerId) clearInterval(timerId);
        };
    }, [detailsFetched, detailsLoading, errorMessage, successMessage, remainingTime]);

    // Format remaining time as MM:SS
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
    };

    // Clear error on OTP input change
    useEffect(() => {
        if (paymentToken !== '') {
            if (showError && errorMessage.includes("Verification failed")) {
                setShowError(false);
                setErrorMessage('');
            }
        }
    }, [paymentToken]);

    // Handle OTP form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (detailsLoading) {
            setShowError(true); setErrorMessage("Payment details still loading."); return;
        }
        if (errorMessage && !showError && !errorMessage.includes("Verification failed")) {
            setShowError(true); return;
        }
        if (!paymentToken || paymentToken.length !== 6) {
            setShowError(true); setErrorMessage("Please enter the 6-digit verification code."); return;
        }
        if (remainingTime === 0 && !successMessage) {
            setShowError(true); setErrorMessage('Verification time expired. Please restart the payment.'); return;
        }
        setIsLoading(true);
        setShowError(false);
        setErrorMessage('');
        try {
            const verification = new PaymentVerification(paymentToken);
            const result = await VerificationService.verifyPayment(verification);
            if (result.success) {
                setShowError(false);
                setSuccessMessage(result.message || 'Payment successful! Redirecting...');
                sessionStorage.removeItem('pendingPaymentId');
                setRemainingTime(0);
                setTimeout(() => { window.location.href = '/payment-success'; }, 3000);
            } else {
                setShowError(true);
                setErrorMessage(result.message || 'Verification failed. Please check the code and try again.');
                setPaymentToken('');
            }
        } catch (error) {
            setShowError(true);
            setErrorMessage('An error occurred while contacting the server. Please try again.');
            setPaymentToken('');
        } finally {
            if (!successMessage) {
                setIsLoading(false);
            }
        }
    };

    // Disable form during loading, timeout, success, or initial error
    const isFormDisabled = isLoading || detailsLoading || (remainingTime === 0 && !successMessage) || successMessage !== null || (errorMessage && !showError && !errorMessage.includes("Verification failed"));

    // Render OTP form and payment details
    return (
        <div className="verification-container">
            <div className="verification-box">
                <div className="logo-container">
                    <div className="left-logo"><img src={MASIB} alt="Bank Logo"/></div>
                </div>
                {detailsLoading ? (
                    <p>Loading payment details...</p>
                ) : errorMessage && !showError && !errorMessage.includes("Verification failed") ? (
                    <p className="error-message">{errorMessage}</p>
                ) : (
                    <>
                        {!showError && !successMessage && remainingTime > 0 &&
                            <>
                                <p>A verification code has been sent to your registered phone number.</p>
                                <p>Time remaining: {formatTime(remainingTime)}</p>
                            </>
                        }
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Merchant:</label>
                                <input type="text" value={paymentDetails.merchantName} disabled/>
                            </div>
                            <div className="form-group">
                                <label>Amount:</label>
                                <input type="text" value={paymentDetails.amount} disabled/>
                            </div>
                            <div className="form-group">
                                <label>Card:</label>
                                <input type="text" value={paymentDetails.cardNumber} disabled/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="otpCode">Enter the 6-digit code:</label>
                                <input
                                    id="otpCode"
                                    type="text"
                                    inputMode="numeric"
                                    pattern="\d{6}"
                                    placeholder="Enter code"
                                    value={paymentToken}
                                    onChange={(e) => setPaymentToken(e.target.value.replace(/\D/g, '').substring(0, 6))}
                                    disabled={isFormDisabled}
                                    maxLength={6}
                                    autoFocus
                                    required
                                />
                            </div>
                            <button type="submit" disabled={isFormDisabled}>
                                {isLoading ? 'Verifying...' : 'Confirm Payment'}
                            </button>
                        </form>
                    </>
                )}
                {showError && <p className="error-message">{errorMessage}</p>}
                {successMessage && (
                    <div className="success-message">
                        <p><strong>{successMessage}</strong></p>
                    </div>
                )}
                <div className="logo-container">
                    <div className="right-logos">
                        <img src={VISA} alt="Verified by Visa"/>
                        <img src={MASTC} alt="MasterCard SecureCode"/>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentVerificationPage;