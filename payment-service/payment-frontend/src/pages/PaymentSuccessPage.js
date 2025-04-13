import React, { useEffect } from 'react';
import { AuthService } from '../services/AuthService';
import SPMB from '../images/SMPB.png';

const PaymentSuccessPage = () => {
    useEffect(() => {
        // Check if user is authenticated
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }
    }, []);

    // Handle return to store
    const handleReturnToStore = () => {
        window.location.href = '/';
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

                    <div className="form-group">
                        <label>Time:</label>
                        <input type="text" value={new Date().toLocaleTimeString()} disabled />
                    </div>
                </div>

                <button onClick={handleReturnToStore}>
                    Return to Store
                </button>
            </div>
        </div>
    );
};

export default PaymentSuccessPage;