import React, { useState } from 'react';
import './App.css';

const PaymentForm = () => {
  const [formData, setFormData] = useState({
    cardPart1: '',
    cardPart2: '',
    cardPart3: '',
    cardPart4: '',
    expiry: '',
    cvc: ''
  });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    // Limit to 4 digits per part
    if (value.length <= 4 && /^\d*$/.test(value)) {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const validateForm = () => {
    let tempErrors = {};
    const fullCardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`;

    if (!fullCardNumber.match(/^\d{16}$/)) {
      tempErrors.cardNumber = 'Card number must be 16 digits';
    }
    if (!formData.expiry.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
      tempErrors.expiry = 'Invalid expiry date (MM/YY)';
    }
    if (!formData.cvc.match(/^\d{3,4}$/)) {
      tempErrors.cvc = 'CVC must be 3 or 4 digits';
    }

    setErrors(tempErrors);
    return Object.keys(tempErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const cardNumber = `${formData.cardPart1}${formData.cardPart2}${formData.cardPart3}${formData.cardPart4}`;

    try {
      const response = await fetch('http://localhost:8443/api/process-payment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Basic ' + btoa('admin:admin123') // À remplacer par un vrai système d'authent
        },
        body: JSON.stringify({
          cardNumber,
          expiry: formData.expiry,
          cvc: formData.cvc,
          amount: 5.39
        })
      });

      if (response.ok) {
        alert('Paiement réussi !');
      } else {
        alert('Échec du paiement');
      }
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  return (
      <div className="container">
        <div className="left-logo">
          <img src="/images/MASIB.png" alt="Bank Logo" />
        </div>

        <div className="amount">5.39 USD</div>
        <div className="merchant">Grapes</div>

        <form onSubmit={handleSubmit}>
          <div className="input-container card-number-group">
            <input
                type="text"
                name="cardPart1"
                value={formData.cardPart1}
                onChange={handleChange}
                maxLength="4"
                placeholder="XXXX"
                className="card-part"
            />
            <input
                type="text"
                name="cardPart2"
                value={formData.cardPart2}
                onChange={handleChange}
                maxLength="4"
                placeholder="XXXX"
                className="card-part"
            />
            <input
                type="text"
                name="cardPart3"
                value={formData.cardPart3}
                onChange={handleChange}
                maxLength="4"
                placeholder="XXXX"
                className="card-part"
            />
            <input
                type="text"
                name="cardPart4"
                value={formData.cardPart4}
                onChange={handleChange}
                maxLength="4"
                placeholder="XXXX"
                className="card-part"
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
            />
            <span className="required">*</span>
            {errors.cvc && <span className="error">{errors.cvc}</span>}
          </div>

          <button type="submit">Pay 5.39 USD</button>
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

export default PaymentForm;