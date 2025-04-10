import React, { useState } from 'react';
import '../global.css';
import MASIB from '../images/MASIB.png';
import VISA from '../images/verified_by_visa.png';
import MASTC from '../images/mastercardsecurecode_logo.png';
import { PaymentVerification } from '../models/PaymentVerification';
import { VerificationService } from '../services/VerificationService';

const PaymentVerificationPage = () => {
    // State for OTP input and status messages
    const [paymentToken, setPaymentToken] = useState('');
    const [showError, setShowError] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    // Handle verification form submission
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic validation
        if (paymentToken.trim() === "") {
            alert("Please enter the received code.");
            return;
        }

        try {
            // Create verification object and submit to service
            const verification = new PaymentVerification(paymentToken);
            const success = await VerificationService.verifyPayment(verification);

            if (success) {
                // Handle successful verification
                setShowError(false);
                setSuccessMessage(paymentToken);
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
            } else {
                // Show error on failure
                setShowError(true);
            }
        } catch (error) {
            console.error('Error submitting payment:', error);
            setShowError(true);
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
                        <img src={MASTC} alt="MasterCard" />
                    </div>
                </div>

                <p>A verification code has been sent to your registered number.</p>

                <form onSubmit={handleSubmit}>
                    {/* Transaction details display (non-editable) */}
                    <div className="form-group">
                        <label>Merchant name:</label>
                        <input type="text" value="Grapes" disabled />
                    </div>

                    <div className="form-group">
                        <label>Amount of the transaction:</label>
                        <input type="text" value="EURO 45.99 €" disabled />
                    </div>

                    <div className="form-group">
                        <label>Card number:</label>
                        <input type="text" value="XXXX XXXX XXXX 0237" disabled />
                    </div>

                    {/* OTP input field */}
                    <div className="form-group">
                        <label>Enter the received code:</label>
                        <input
                            type="text"
                            placeholder="Enter the OTP code here"
                            value={paymentToken}
                            onChange={(e) => setPaymentToken(e.target.value)}
                        />
                    </div>

                    <button type="submit">Confirm</button>
                </form>

                {/* Error message display */}
                {showError && (
                    <p className="error-message">
                        Payment failed, the code could not be verified.
                    </p>
                )}

                {/* Success message with redirect notification */}
                {successMessage && (
                    <div className="success-message">
                        <p>
                            Payment successful with code: <strong>{successMessage}</strong>.<br />
                            Redirecting to homepage in 3 seconds...
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentVerificationPage;