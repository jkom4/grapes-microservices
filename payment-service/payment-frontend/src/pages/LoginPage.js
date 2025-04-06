// src/pages/LoginPage.js
import React, { useState, useEffect } from 'react'; // Import React and hooks for state and side effects
import '../global.css'; // Import global CSS for styling
import SPMB from '../images/SPMB.png'; // Import logo image for display

const LoginPage = () => {
    // State to store login form data (username and password)
    const [loginData, setLoginData] = useState({ login: '', password: '' });

    // State to manage password validation rules and visibility
    const [passwordValidation, setPasswordValidation] = useState({
        hasLowerAndUpper: false, // Tracks if password has both lower and upper case letters
        hasNumberOrSymbol: false, // Tracks if password has a number or special symbol
        isLongEnough: false, // Tracks if password is at least 8 characters
        showValidation: false // Controls whether validation feedback is shown
    });

    // State to display error messages from login attempts
    const [errorMessage, setErrorMessage] = useState('');

    // Handle input changes for username and password fields
    const handleChange = (e) => {
        const { id, value } = e.target; // Extract field id and value from event
        setLoginData(prev => ({ ...prev, [id]: value })); // Update loginData state with new value
        setErrorMessage(''); // Clear any previous error message on input change
        if (id === 'password') {
            // Show password validation feedback when user types in password field
            setPasswordValidation(prev => ({ ...prev, showValidation: true }));
        }
    };

    // Effect to validate password requirements whenever it changes
    useEffect(() => {
        if (loginData.password) { // Only run validation if password is not empty
            const hasLowerAndUpper = /(?=.*[a-z])(?=.*[A-Z])/.test(loginData.password); // Check for lower and upper case
            const hasNumberOrSymbol = /(?=.*\d)|(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?])/.test(loginData.password); // Check for number or symbol
            const isLongEnough = loginData.password.length >= 8; // Check length requirement
            // Update password validation state with results
            setPasswordValidation(prev => ({
                ...prev,
                hasLowerAndUpper,
                hasNumberOrSymbol,
                isLongEnough
            }));
        }
    }, [loginData.password]); // Dependency array ensures this runs when password changes

    // Handle form submission for login
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission behavior
        try {
            // Send login request to the server
            const response = await fetch('http://localhost:8443/api/login', {
                method: 'POST', // HTTP method for sending data
                mode: 'no-cors', // Disables CORS (not recommended for production)
                headers: { 'Content-Type': 'application/json' }, // Specify JSON content type
                body: JSON.stringify({
                    username: loginData.login, // Map login field to username
                    password: loginData.password // Send password as-is
                })
            });

            const data = await response.json(); // Parse response as JSON

            if (response.ok) {
                // If login is successful, redirect to the provided URL
                window.location.href = data.redirectUrl; // Redirect to payment frontend
            } else {
                // Display error message from server if login fails
                setErrorMessage(data.error);
            }
        } catch (error) {
            // Handle network or server errors
            setErrorMessage("Erreur de connexion au serveur"); // Set generic error message
        }
    };

    // JSX to render the login form UI
    return (
        <div className="login-container"> {/* Container for styling the login page */}
            <div className="logo-container"> {/* Container for the logo */}
                <img src={SPMB} alt="Bank Logo" /> {/* Display the imported logo */}
            </div>
            <h2>Login</h2> {/* Page title */}
            <form id="loginForm" onSubmit={handleSubmit}> {/* Form with submit handler */}
                <label htmlFor="login">Username:</label> {/* Username field label */}
                <input
                    type="text" // Text input for username
                    id="login" // ID matches handleChange logic
                    placeholder="Enter your username" // Placeholder text
                    value={loginData.login} // Controlled input value
                    onChange={handleChange} // Update state on change
                    required // Field is mandatory
                />

                <label htmlFor="password">Password:</label> {/* Password field label */}
                <input
                    type="password" // Password input hides characters
                    id="password" // ID matches handleChange logic
                    placeholder="Enter your password" // Placeholder text
                    value={loginData.password} // Controlled input value
                    onChange={handleChange} // Update state on change
                    required // Field is mandatory
                    className={passwordValidation.showValidation ? // Conditional styling based on validation
                        ((!passwordValidation.hasLowerAndUpper || !passwordValidation.hasNumberOrSymbol || !passwordValidation.isLongEnough)
                            ? 'password-error' : 'password-valid') : ''} // Apply error or valid class
                />

                {/* Show password validation feedback if enabled */}
                {passwordValidation.showValidation && (
                    <div className="password-validation"> {/* Container for validation messages */}
                        <div className="lock-icon"> {/* Lock icon for visual feedback */}
                            <i className="fa-solid fa-lock"></i> {/* FontAwesome lock icon */}
                        </div>
                        <p>Your password needs to:</p> {/* Instruction text */}
                        <ul> {/* List of validation criteria */}
                            <li className={passwordValidation.hasLowerAndUpper ? 'valid' : 'invalid'}> {/* Style based on condition */}
                                <span>
                                    {passwordValidation.hasLowerAndUpper ? ( // Checkmark if valid
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : ( // Cross if invalid
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                include both lower and upper case characters. {/* Requirement text */}
                            </li>
                            <li className={passwordValidation.hasNumberOrSymbol ? 'valid' : 'invalid'}> {/* Style based on condition */}
                                <span>
                                    {passwordValidation.hasNumberOrSymbol ? ( // Checkmark if valid
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : ( // Cross if invalid
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                include at least one number or symbol. {/* Requirement text */}
                            </li>
                            <li className={passwordValidation.isLongEnough ? 'valid' : 'invalid'}> {/* Style based on condition */}
                                <span>
                                    {passwordValidation.isLongEnough ? ( // Checkmark if valid
                                        <svg className="checkmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#4caf50" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                                        </svg>
                                    ) : ( // Cross if invalid
                                        <svg className="crossmark" width="16" height="16" viewBox="0 0 24 24">
                                            <path fill="#ff3333" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                                        </svg>
                                    )}
                                </span>
                                be at least 8 characters long. {/* Requirement text */}
                            </li>
                        </ul>
                    </div>
                )}

                <button type="submit">Login</button> {/* Submit button for the form */}
            </form>
            {/* Display error message if it exists */}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
};

export default LoginPage; // Export the component for use in other parts of the app