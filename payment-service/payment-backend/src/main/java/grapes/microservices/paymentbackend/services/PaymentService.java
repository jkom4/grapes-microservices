package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.CardDetails;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.CardDetailsRepository;
import grapes.microservices.paymentbackend.utils.SslUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Optional;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID; // Import UUID

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CardDetailsRepository cardDetailsRepository;

    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.truststore.acq.path}")
    private String acqTruststorePath;

    @Value("${app.truststore.acq.password}")
    private String acqTruststorePassword;

    /**
     * Traite la complétion du paiement après vérification OTP.
     * *** NOUVELLE VÉRIFICATION AVEC EQUALS() ***
     *
     * @param token Le token OTP soumis par l'utilisateur.
     * @param paymentRequest Le DTO contenant les détails de la carte (récupéré de la session).
     * @param user L'utilisateur authentifié.
     * @return true si le paiement est vérifié et traité avec succès, false sinon.
     */
    public boolean processPayment(String token, PaymentRequestDTO paymentRequest, User user) {
        String transactionContextId = "paymentAttempt-" + UUID.randomUUID().toString();
        log.info("Processing payment completion for user {} with token {} for transaction context {}", user.getLogin(), token, transactionContextId);
        saveCardDetails(paymentRequest, user);

        try {
            String acqResponse = sendTokenToAcq(token);

            // --- VÉRIFICATION ACK/NACK - VERSION STRICTE AVEC EQUALS ---
            log.info("PaymentService: Checking ACQ response for transaction context {}. Raw response: '{}'", transactionContextId, acqResponse);

            // Définir la chaîne exacte attendue pour un succès
            String expectedAckResponse = "Response from ACQ: ACK";
            boolean isSuccessResponse = false; // Par défaut à false

            if (acqResponse != null) {
                // Comparaison exacte après avoir retiré les espaces potentiels au début/fin
                isSuccessResponse = acqResponse.trim().equals(expectedAckResponse);
                log.info("PaymentService: Does the response ('{}') exactly match the expected ACK ('{}')? {} (Context: {})",
                        acqResponse, expectedAckResponse, isSuccessResponse, transactionContextId);
            } else {
                log.warn("PaymentService: Received null response from ACQ. (Context: {})", transactionContextId);
            }

            boolean isVerified = isSuccessResponse; // isVerified dépend maintenant de l'égalité exacte
            log.info("PaymentService: Final 'isVerified' flag determined as: {} for transaction context {}", isVerified, transactionContextId);
            // --- FIN VÉRIFICATION STRICTE ---

            if (isVerified) { // N'entrera ici QUE si la réponse est EXACTEMENT "Response from ACQ: ACK"
                log.info("OTP '{}' successfully verified by ACS/ACQ for user {} and transaction context {}", token, user.getLogin(), transactionContextId);
                log.info("RABBITMQ: Message 'payment_success' would be sent here for transaction context {}", transactionContextId);
                log.info("Payment successful for user {} and transaction context {}", user.getLogin(), transactionContextId);
                return true;
            } else {
                log.warn("Payment verification failed for user {}. OTP submitted: '{}'. ACQ/ACS raw response: '{}'. Transaction context {}",
                        user.getLogin(), token, acqResponse, transactionContextId);
                return false; // Échec car la réponse n'était pas EXACTEMENT "Response from ACQ: ACK"
            }
        } catch (Exception e) {
            log.error("Error processing payment completion for transaction context {}: {}", transactionContextId, e.getMessage(), e);
            return false;
        }
    }

    // --- Les autres méthodes (sendTokenToAcq, saveCardDetails, maskCardNumber, validateCardNumber, etc.) restent inchangées par rapport à la version précédente ---

    /**
     * Envoie le token OTP à l'ACQ pour vérification.
     */
    private String sendTokenToAcq(String token) throws Exception {
        log.info("Sending token to ACQ for verification: {}", token); // Log uniquement le token
        try (SSLSocket acqSocket = SslUtils.createSslClientSocket(
                acqPort, acqTruststorePath, acqTruststorePassword)) {
            PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));

            writer.println(token); // Envoie JUSTE le token brut
            log.info("Token sent to ACQ");

            String response = reader.readLine();
            log.info("Received raw response from ACQ: {}", response); // Log la réponse brute
            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACQ: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sauvegarde ou met à jour les détails de carte associés à un utilisateur.
     */
    private CardDetails saveCardDetails(PaymentRequestDTO paymentRequest, User user) {
        Optional<CardDetails> existingCard = cardDetailsRepository.findByCardNumberAndUserId(
                paymentRequest.getCardNumber(), user.getId());

        if (existingCard.isPresent()) {
            CardDetails card = existingCard.get();
            card.setExpirationDate(paymentRequest.getExpirationDate());
            card.setCvv(paymentRequest.getCvv());
            String userNameFromUserObject = user.getLogin();
            card.setCardDetails(String.format(
                    "{\"cardNumber\":\"%s\",\"expirationDate\":\"%s\",\"retrievedUserName\":\"%s\"}",
                    maskCardNumber(paymentRequest.getCardNumber()),
                    paymentRequest.getExpirationDate(),
                    userNameFromUserObject
            ));
            card.setCardNumber(paymentRequest.getCardNumber()); // Assure la persistance
            log.info("Updating existing card details entry (ID: {}) for user ID: {}", card.getId(), user.getId());
            return cardDetailsRepository.save(card);
        } else {
            CardDetails newCard = new CardDetails(
                    paymentRequest.getCardNumber(),
                    paymentRequest.getExpirationDate(),
                    paymentRequest.getCvv(),
                    user
            );
            newCard.setCardNumber(paymentRequest.getCardNumber()); // Assure la persistance
            if (newCard.getUserId() == null && user != null) {
                newCard.setUserId(user.getId());
            }
            log.info("Saving new card details for user ID: {}", newCard.getUserId());
            return cardDetailsRepository.save(newCard);
        }
    }

    /**
     * Masque un numéro de carte pour l'affichage ou le stockage partiel.
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return cardNumber != null ? cardNumber : "****";
        }
        return "XXXXXXXXXXXX" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Valide le format et l'algorithme de Luhn pour un numéro de carte.
     */
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String cleanedCardNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanedCardNumber.length() < 13 || cleanedCardNumber.length() > 19 || !cleanedCardNumber.matches("\\d+")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanedCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanedCardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    /**
     * Vérifie si le montant du paiement est dans une limite acceptable.
     */
    public boolean isAmountAcceptable(BigDecimal amount) {
        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.compareTo(new BigDecimal("10000")) < 0;
    }

    /**
     * Vérifie si la date d'expiration de la carte est valide (format MM/YYYY) et non expirée.
     */
    public boolean isExpirationDateValid(String expirationDateStr) {
        if (expirationDateStr == null || !expirationDateStr.matches("^(0[1-9]|1[0-2])/20[2-9][0-9]$")) {
            log.warn("Invalid expiration date format received: {}", expirationDateStr);
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth expirationYearMonth = YearMonth.parse(expirationDateStr, formatter);
            YearMonth currentYearMonth = YearMonth.now(ZoneId.systemDefault());
            return !expirationYearMonth.isBefore(currentYearMonth);
        } catch (DateTimeParseException e) {
            log.error("Error parsing expiration date: {}", expirationDateStr, e);
            return false;
        }
    }
} // Fin de la classe