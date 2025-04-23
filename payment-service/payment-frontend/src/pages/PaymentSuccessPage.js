import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { AuthService } from '../services/AuthService';
import SPMB from '../images/SMPB.png';

// Displays confirmation for a successful payment
const PaymentSuccessPage = () => {
    // Redirect to login if not authenticated
    useEffect(() => {
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }
    }, []);


    // Render success message and payment details
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
                </div>
                <Link to="/">
                    <button>Return to Store</button>
                </Link>
            </div>
        </div>
    );
};

export default PaymentSuccessPage;