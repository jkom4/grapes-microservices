import React, { useState, useEffect } from 'react';
import '../global.css';
import SPMB from '../images/SMPB.png';
import { AuthService } from '../services/AuthService';
import { User } from '../models/User';

// Handles user authentication and payment-specific login
const LoginPage = () => {
    // State for paymentId, form inputs, validation, errors, and loading
    const [paymentId, setPaymentId] = useState(null);
    const [loginData, setLoginData] = useState({ login: '', password: '' });
    const [passwordValidation, setPasswordValidation] = useState({
        hasLowerAndUpper: false,
        hasNumberOrSymbol: false,
        isLongEnough: false,
        showValidation: false
    });
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // Extract paymentId from URL on mount
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const paymentIdParam = urlParams.get('paymentId');
        if (paymentIdParam) {
            setPaymentId(paymentIdParam);
        }
    }, []);

    // Update form inputs and clear errors
    const handleChange = (e) => {
        const { id, value } = e.target;
        setLoginData(prev => ({ ...prev, [id]: value }));
        setErrorMessage('');
        if (id === 'password') {
            setPasswordValidation(prev => ({ ...prev, showValidation: true }));
        }
    };

    // Validate password requirements on change
    useEffect(() => {
        if (loginData.password) {
            const hasLowerAndUpper = /(?=.*[a-z])(?=.*[A-Z])/.test(loginData.password);
            const hasNumberOrSymbol = /(?=.*\d)|(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?])/.test(loginData.password);
            const isLongEnough = loginData.password.length >= 8;
            setPasswordValidation(prev => ({
                ...prev,
                hasLowerAndUpper,
                hasNumberOrSymbol,
                isLongEnough
            }));
        }
    }, [loginData.password]);

    // Handle login submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            if (passwordValidation.showValidation &&
                (!passwordValidation.hasLowerAndUpper ||
                    !passwordValidation.hasNumberOrSymbol ||
                    !passwordValidation.isLongEnough)) {
                setErrorMessage("Password does not meet security requirements");
                setIsLoading(false);
                return;
            }
            const user = new User(loginData.login, loginData.password);
            const data = await AuthService.login(user, paymentId);
            if (data.success) {
                window.location.href = data.redirectUrl || '/dashboard';
            } else {
                setErrorMessage(data.error || "Authentication failed");
            }
        } catch (error) {
            setErrorMessage("Connection to server failed. Please try again later.");
        } finally {
            setIsLoading(false);
        }
    };

    // Render login form and UI
    return (
        <div className="login-container">
            <div className="logo-container">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <h2>Login to Your Account</h2>
            {paymentId && (
                <div className="payment-notice">
                    <p>Payment authentication required</p>
                </div>
            )}
            <form id="loginForm" onSubmit={handleSubmit}>
                <label htmlFor="login">Username:</label>
                <input
                    type="text"
                    id="login"
                    placeholder="Enter your username"
                    value={loginData.login}
                    onChange={handleChange}
                    disabled={isLoading}
                    required
                />
                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    placeholder="Enter your password"
                    value={loginData.password}
                    onChange={handleChange}
                    disabled={isLoading}
                    required
                    className={passwordValidation.showValidation ?
                        ((!passwordValidation.hasLowerAndUpper || !passwordValidation.hasNumberOrSymbol || !passwordValidation.isLongEnough)
                            ? 'password-error' : 'password-valid') : ''}
                />
                {passwordValidation.showValidation && (
                    <div className="password-validation">
                        <div className="lock-icon">
                            <i className="fa-solid fa-lock"></i>
                        </div>
                        <p>Your password needs to:</p>
                        <ul>
                            <li className={passwordValidation.hasLowerAndUpper ? 'valid' : 'invalid'}>
                                <span>
                                    {passwordValidation.hasLowerAndUpper ? (
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : (
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                include both lower and upper case characters.
                            </li>
                            <li className={passwordValidation.hasNumberOrSymbol ? 'valid' : 'invalid'}>
                                <span>
                                    {passwordValidation.hasNumberOrSymbol ? (
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : (
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                include at least one number or symbol.
                            </li>
                            <li className={passwordValidation.isLongEnough ? 'valid' : 'invalid'}>
                                <span>
                                    {passwordValidation.isLongEnough ? (
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : (
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                be at least 8 characters long.
                            </li>
                        </ul>
                    </div>
                )}
                <button type="submit" disabled={isLoading}>{isLoading ? 'Logging in...' : 'Login'}</button>
            </form>
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            <div className="security-notice">
                <p>This is a secure 3D authentication system for your bank account</p>
                <p>Your security is our priority</p>
            </div>
        </div>
    );
};

export default LoginPage;