import React, { useState, useEffect } from 'react';
import '../global.css';
import SPMB from '../images/SMPB.png';
import { AuthService } from '../services/AuthService';
import { User } from '../models/User';

const LoginPage = () => {
    // State for form inputs and validation
    const [loginData, setLoginData] = useState({ login: '', password: '' });
    const [passwordValidation, setPasswordValidation] = useState({
        hasLowerAndUpper: false,
        hasNumberOrSymbol: false,
        isLongEnough: false,
        showValidation: false
    });
    const [errorMessage, setErrorMessage] = useState('');

    // Handle input changes and reset error message
    const handleChange = (e) => {
        const { id, value } = e.target;
        setLoginData(prev => ({ ...prev, [id]: value }));
        setErrorMessage('');

        if (id === 'password') {
            setPasswordValidation(prev => ({ ...prev, showValidation: true }));
        }
    };

    // Check password requirements whenever password changes
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

    // Login form submission handler
    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            // Attempt login with provided credentials
            const user = new User(loginData.login, loginData.password);
            const data = await AuthService.login(user);

            // Handle successful login or error
            if (data.redirectUrl) {
                window.location.href = data.redirectUrl;
            } else {
                setErrorMessage(data.error);
            }
        } catch (error) {
            setErrorMessage("Connection to server failed");
        }
    };

    return (
        <div className="login-container">
            <div className="logo-container">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <h2>Login</h2>
            <form id="loginForm" onSubmit={handleSubmit}>
                {/* Username input field */}
                <label htmlFor="login">Username:</label>
                <input
                    type="text"
                    id="login"
                    placeholder="Enter your username"
                    value={loginData.login}
                    onChange={handleChange}
                    required
                />

                {/* Password input with dynamic styling based on validation */}
                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    placeholder="Enter your password"
                    value={loginData.password}
                    onChange={handleChange}
                    required
                    className={passwordValidation.showValidation ?
                        ((!passwordValidation.hasLowerAndUpper || !passwordValidation.hasNumberOrSymbol || !passwordValidation.isLongEnough)
                            ? 'password-error' : 'password-valid') : ''}
                />

                {/* Password validation requirements display */}
                {passwordValidation.showValidation && (
                    <div className="password-validation">
                        <div className="lock-icon">
                            <i className="fa-solid fa-lock"></i>
                        </div>
                        <p>Your password needs to:</p>
                        <ul>
                            {/* Check for lowercase and uppercase */}
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
                            {/* Check for numbers or symbols */}
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
                            {/* Check for minimum length */}
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

                <button type="submit">Login</button>
            </form>
            {/* Display error messages */}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
};

export default LoginPage;