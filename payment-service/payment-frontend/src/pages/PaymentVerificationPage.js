// --- START OF src/pages/PaymentVerificationPage.js ---
import React, { useState, useEffect } from 'react';
// Assurez-vous que les chemins d'importation des images sont corrects
import MASIB from '../images/MASIB.png';
import VISA from '../images/verified_by_visa.png';
import MASTC from '../images/mastercardsecurecode_logo.png';
// Importez les modèles et services nécessaires
import { PaymentVerification } from '../models/PaymentVerification'; // Assurez-vous que le modèle est correct
import { VerificationService } from '../services/VerificationService';
import { AuthService } from '../services/AuthService'; // Pour vérifier la connexion

const PaymentVerificationPage = () => {
    // États pour le formulaire et le processus
    const [paymentToken, setPaymentToken] = useState(''); // Le code OTP entré par l'utilisateur
    const [showError, setShowError] = useState(false); // Pour afficher/masquer le message d'erreur
    const [errorMessage, setErrorMessage] = useState(''); // Le message d'erreur à afficher
    const [successMessage, setSuccessMessage] = useState(null); // Message en cas de succès
    const [paymentDetails, setPaymentDetails] = useState({ // Détails du paiement (marchand, montant, carte masquée)
        merchantName: 'Loading...',
        amount: 'Loading...',
        cardNumber: 'Loading...'
    });
    const [isLoading, setIsLoading] = useState(false); // Pour désactiver le bouton pendant la soumission
    const [detailsLoading, setDetailsLoading] = useState(true); // Pour indiquer le chargement initial des détails
    const [remainingTime, setRemainingTime] = useState(180); // Timer de 3 minutes (180 secondes)

    // --- Effet pour charger les détails du paiement et gérer le timer ---
    useEffect(() => {
        let timerId = null; // ID du timer pour pouvoir l'arrêter

        // Fonction asynchrone pour récupérer les détails depuis le backend
        const fetchDetails = async () => {
            setDetailsLoading(true);
            setErrorMessage(''); // Efface les erreurs précédentes
            try {
                const result = await VerificationService.getPendingPaymentDetails();
                if (result.success) {
                    setPaymentDetails(result.details);
                    setErrorMessage(''); // Efface l'erreur si succès
                } else {
                    // Affiche l'erreur renvoyée par le backend
                    setErrorMessage(result.message || "Could not load payment details.");
                    setPaymentDetails({ merchantName: 'N/A', amount: 'N/A', cardNumber: 'N/A' });
                    // Si aucune paiement en attente n'est trouvé, c'est une erreur fatale pour cette page
                    if (result.message && result.message.toLowerCase().includes("no pending payment found")) {
                        // Optionnel: rediriger vers la page de paiement après un délai ?
                        // setTimeout(() => { window.location.href = '/payment'; }, 4000);
                    }
                }
            } catch (error) {
                console.error("Error fetching payment details:", error);
                setErrorMessage("Failed to fetch payment details due to a network or server error.");
                setPaymentDetails({ merchantName: 'Error', amount: 'Error', cardNumber: 'Error' });
            } finally {
                setDetailsLoading(false); // Termine l'état de chargement
            }
        };

        // Vérifie si l'utilisateur est connecté avant de faire quoi que ce soit
        if (!AuthService.isLoggedIn()) {
            window.location.href = '/login'; // Redirige vers le login si pas connecté
            return; // Arrête l'exécution de l'effet
        } else {
            fetchDetails(); // Lance la récupération des détails si connecté
        }

        // Logique du Timer (démarre seulement après le chargement des détails sans erreur et sans succès préalable)
        if (!detailsLoading && !errorMessage && !successMessage) {
            timerId = setInterval(() => {
                setRemainingTime(prevTime => {
                    if (prevTime <= 1) {
                        clearInterval(timerId); // Arrête le timer
                        // Si le temps est écoulé et qu'il n'y a pas de message de succès, affiche l'erreur d'expiration
                        if (!successMessage) {
                            setShowError(true);
                            setErrorMessage('Verification time expired. Please restart the payment.');
                        }
                        return 0; // Met le temps à 0
                    }
                    return prevTime - 1; // Décrémente le temps
                });
            }, 1000); // Met à jour chaque seconde
        } else {
            // Si les détails sont en chargement, qu'il y a une erreur initiale, ou que c'est déjà réussi, on s'assure que le timer est arrêté.
            if(timerId) clearInterval(timerId);
        }

        // Fonction de nettoyage : arrête le timer si le composant est démonté
        return () => {
            if (timerId) clearInterval(timerId);
        };
        // Dépendances : relance l'effet si une de ces valeurs change (ex: succès, erreur initiale)
    }, [successMessage, detailsLoading]); // On enlève errorMessage et paymentToken d'ici pour éviter des redémarrages intempestifs du timer

    // --- Fonction pour formater le temps restant (MM:SS) ---
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`; // Ajoute un zéro devant les secondes si < 10
    };

    // --- Effet pour réinitialiser le message d'erreur quand l'utilisateur tape ---
    useEffect(() => {
        if (paymentToken !== '') { // Si le champ n'est pas vide
            if(showError && errorMessage.includes("Verification failed")){ // Seulement si l'erreur était une tentative échouée
                setShowError(false); // Cache l'erreur
                setErrorMessage(''); // Efface le message
            }
        }
    }, [paymentToken]); // Se déclenche à chaque changement du code OTP

    // --- Fonction pour gérer la soumission du formulaire ---
    const handleSubmit = async (e) => {
        e.preventDefault(); // Empêche le rechargement de la page

        // --- Validations avant de soumettre ---
        if (detailsLoading) { // Ne pas soumettre si les détails chargent encore
            setShowError(true); setErrorMessage("Payment details still loading."); return;
        }
        // Si une erreur de chargement initiale existe ET que ce n'est PAS une erreur de tentative échouée
        if (errorMessage && !showError && !errorMessage.includes("Verification failed")) {
            setShowError(true); /* Affiche l'erreur existante */ return;
        }
        if (!paymentToken || paymentToken.length !== 6) { // Vérifie si le code est bien de 6 chiffres
            setShowError(true); setErrorMessage("Please enter the 6-digit verification code."); return;
        }
        // Vérifie si le timer est expiré (et qu'on n'a pas déjà réussi)
        if (remainingTime === 0 && !successMessage) {
            setShowError(true); setErrorMessage('Verification time expired. Please restart the payment.'); return;
        }
        // -----------------------------------------

        setIsLoading(true); // Désactive le bouton
        setShowError(false); // Cache les erreurs précédentes au début de la tentative
        setErrorMessage(''); // Efface le message d'erreur précédent

        try {
            // Crée l'objet pour l'API avec le code OTP
            const verification = new PaymentVerification(paymentToken);
            // Appelle le service de vérification du backend
            const result = await VerificationService.verifyPayment(verification);

            if (result.success) {
                // --- CAS DE SUCCÈS ---
                setShowError(false); // Assure qu'aucune erreur n'est montrée
                setSuccessMessage(result.message || 'Payment successful! Redirecting...'); // Affiche message de succès
                sessionStorage.removeItem('pendingPaymentId'); // Nettoie l'ID de paiement en attente (si utilisé)
                setRemainingTime(0); // Arrête le timer visuellement
                // Redirige vers la page de succès après un court délai
                setTimeout(() => { window.location.href = '/payment-success'; }, 3000);

            } else {
                // --- CAS D'ÉCHEC (ex: mauvais OTP) ---
                // Le backend renvoie success: false et un message
                setShowError(true); // Affiche la zone d'erreur
                // Affiche le message spécifique renvoyé par le backend
                setErrorMessage(result.message || 'Verification failed. Please check the code and try again.');
                setPaymentToken(''); // Vide le champ OTP pour permettre une nouvelle saisie !
                // Le formulaire reste actif pour une nouvelle tentative.
            }
        } catch (error) {
            // --- CAS D'ERREUR TECHNIQUE (réseau, serveur...) ---
            console.error('Error submitting verification:', error);
            setShowError(true); // Affiche la zone d'erreur
            setErrorMessage('An error occurred while contacting the server. Please try again.'); // Message générique
            setPaymentToken(''); // Vide aussi le champ en cas d'erreur serveur pour retenter
        } finally {
            // Réactive le bouton si ce n'est pas un succès (car en cas de succès, on redirige)
            if (!successMessage) {
                setIsLoading(false);
            }
        }
    };

    // --- Variable pour déterminer si le formulaire doit être désactivé ---
    // Désactivé si :
    // - En cours de chargement des détails (detailsLoading)
    // - En cours de soumission (isLoading)
    // - Le timer est à zéro ET on n'a pas déjà réussi (échec définitif par expiration)
    // - Le paiement a déjà réussi (successMessage n'est pas null)
    // - Il y a une erreur de chargement initiale (et pas une erreur de tentative)
    const isFormDisabled = isLoading || detailsLoading || (remainingTime === 0 && !successMessage) || successMessage !== null || (errorMessage && !showError && !errorMessage.includes("Verification failed"));

    // --- Rendu du composant ---
    return (
        <div className="verification-container">
            <div className="verification-box">
                {/* Section des logos */}
                <div className="logo-container">
                    <div className="left-logo"><img src={MASIB} alt="Bank Logo" /></div>
                    <div className="right-logos">
                        <img src={VISA} alt="Verified by Visa" />
                        <img src={MASTC} alt="MasterCard SecureCode" />
                    </div>
                </div>

                {/* Affichage pendant le chargement des détails */}
                {detailsLoading ? (
                    <p>Loading payment details...</p>
                    // Affiche l'erreur de chargement initiale si elle existe et que ce n'est pas une erreur de tentative
                ) : errorMessage && !showError && !errorMessage.includes("Verification failed") ? (
                    <p className="error-message">{errorMessage}</p>
                    // Affiche le formulaire et les infos si le chargement est terminé et qu'il n'y a pas d'erreur initiale bloquante
                ) : (
                    <>
                        {/* Affiche le message d'instruction et le timer seulement si pas d'erreur de tentative et pas de succès */}
                        {!showError && !successMessage && remainingTime > 0 &&
                            <>
                                <p>A verification code has been sent to your registered phone number.</p>
                                <p>Time remaining: {formatTime(remainingTime)}</p>
                            </>
                        }

                        {/* Le formulaire de saisie OTP */}
                        <form onSubmit={handleSubmit}>
                            {/* Champs d'information (désactivés) */}
                            <div className="form-group">
                                <label>Merchant:</label>
                                <input type="text" value={paymentDetails.merchantName} disabled />
                            </div>
                            <div className="form-group">
                                <label>Amount:</label>
                                <input type="text" value={paymentDetails.amount} disabled />
                            </div>
                            <div className="form-group">
                                <label>Card:</label>
                                <input type="text" value={paymentDetails.cardNumber} disabled />
                            </div>
                            {/* Champ de saisie OTP */}
                            <div className="form-group">
                                <label htmlFor="otpCode">Enter the 6-digit code:</label>
                                <input
                                    id="otpCode"
                                    type="text" // Ou "tel" pour clavier numérique sur mobile
                                    inputMode="numeric"
                                    pattern="\d{6}" // Pattern HTML5 pour validation (optionnel)
                                    placeholder="Enter code"
                                    value={paymentToken} // Lié à l'état React
                                    onChange={(e) => setPaymentToken(e.target.value.replace(/\D/g, '').substring(0, 6))} // Garde seulement les chiffres, max 6
                                    disabled={isFormDisabled} // Désactivé selon la logique définie
                                    maxLength={6}
                                    autoFocus // Met le focus ici au chargement
                                    required // Champ requis par HTML5
                                />
                            </div>
                            {/* Bouton de soumission */}
                            <button type="submit" disabled={isFormDisabled}>
                                {isLoading ? 'Verifying...' : 'Confirm Payment'}
                            </button>
                        </form>
                    </>
                )}

                {/* Affiche les messages d'erreur (tentative échouée, expiration, validation locale) */}
                {showError && <p className="error-message">{errorMessage}</p>}

                {/* Affiche le message de succès */}
                {successMessage && (
                    <div className="success-message">
                        <p><strong>{successMessage}</strong></p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentVerificationPage;
// --- END OF src/pages/PaymentVerificationPage.js ---