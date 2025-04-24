import React, { useEffect, useState } from 'react';
import { AuthService } from '../services/AuthService';
import { VerificationService } from '../services/VerificationService';
import SPMB from '../images/SMPB.png';

const PaymentSuccessPage = () => {
    const [redirectCountdown, setRedirectCountdown] = useState(5);
    const [redirectUrl, setRedirectUrl] = useState(null);

    useEffect(() => {
        // Vérifier l'authentification
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }

        // Récupérer l'URL de redirection depuis le stockage de session
        const customRedirectUrl = VerificationService.getRedirectUrl();
        if (customRedirectUrl) {
            setRedirectUrl(customRedirectUrl);

            // Démarrer le compte à rebours pour la redirection
            const timer = setInterval(() => {
                setRedirectCountdown(prev => {
                    if (prev <= 1) {
                        clearInterval(timer);
                        // Rediriger vers l'URL personnalisée et nettoyer
                        VerificationService.clearRedirectUrl();
                        window.location.href = customRedirectUrl;
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }, []);

    const handleReturnClick = () => {
        // Si une URL de redirection est présente, l'utiliser
        if (redirectUrl) {
            VerificationService.clearRedirectUrl();
            window.location.href = redirectUrl;
        }
    };

    return (
        <div className="verification-container">
            <div className="verification-box">
                <div className="left-logo">
                    <img src={SPMB} alt="Bank Logo" />
                </div>
                <div className="success-message">
                    <h2>Paiement réussi!</h2>
                    <p>Votre paiement a été traité avec succès.</p>
                    <p>Une confirmation a été envoyée à votre adresse e-mail.</p>
                    <div className="form-group">
                        <label>Statut:</label>
                        <input type="text" value="Terminé" disabled />
                    </div>
                    <div className="form-group">
                        <label>Date:</label>
                        <input type="text" value={new Date().toLocaleDateString()} disabled />
                    </div>

                    {redirectUrl && (
                        <p className="redirect-notice">
                            Redirection automatique dans {redirectCountdown} secondes...
                        </p>
                    )}
                </div>

                <button onClick={handleReturnClick}>
                    {redirectUrl ? 'Retourner à la boutique' : 'Terminer'}
                </button>
            </div>
        </div>
    );
};

export default PaymentSuccessPage;