import React, { useEffect, useState } from 'react';
import { AuthService } from '../services/AuthService';
import { VerificationService } from '../services/VerificationService';
import SPMB from '../images/SMPB.png';

const PaymentSuccessPage = () => {
    const [redirectCountdown, setRedirectCountdown] = useState(5);
    const [redirectUrl, setRedirectUrl] = useState(null);

    useEffect(() => {
        // Check authentication
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }

        // Get redirect URL from session storage
        const customRedirectUrl = VerificationService.getRedirectUrl();
        if (customRedirectUrl) {
            setRedirectUrl(customRedirectUrl);

            // Start countdown for redirection
            const timer = setInterval(() => {
                setRedirectCountdown(prev => {
                    if (prev <= 1) {
                        clearInterval(timer);
                        // Redirect to custom URL and clean up
                        VerificationService.clearRedirectUrl();
                        window.location.href = customRedirectUrl;
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }, []);

    const handleReturnClick = () => {
        // If a redirect URL is present, use it
        if (redirectUrl) {
            VerificationService.clearRedirectUrl();
            window.location.href = redirectUrl;
        }
    };

    return (
        <div className="verification-container">
            <div className="verification-box">
                <div className="left-logo">
                    <img src={SPMB} alt="Bank Logo" />
                </div>
                <div className="success-message">
                    <h2>Payment Successful!</h2>
                    <p>Your payment has been processed successfully.</p>
                    <p>A confirmation has been sent to your email address.</p>
                    <div className="form-group">
                        <label>Status:</label>
                        <input type="text" value="Completed" disabled />
                    </div>
                    <div className="form-group">
                        <label>Date:</label>
                        <input type="text" value={new Date().toLocaleDateString()} disabled />
                    </div>

                    {redirectUrl && (
                        <p className="redirect-notice">
                            Automatic redirection in {redirectCountdown} seconds...
                        </p>
                    )}
                </div>

                <button onClick={handleReturnClick}>
                    {redirectUrl ? 'Return to store' : 'Finish'}
                </button>
            </div>
        </div>
    );
};

export default PaymentSuccessPage;