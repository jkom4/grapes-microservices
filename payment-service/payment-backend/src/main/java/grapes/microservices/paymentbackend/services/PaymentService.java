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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CardDetailsRepository cardDetailsRepository;

    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.truststore.acq.path}")
    private String acqTruststorePath;


    @Value("${app.truststore.acq.password}") // <-- Inject ACQ truststore password
    private String acqTruststorePassword;



    /**
     * Process a payment by sending the verification token to ACQ
     * @param token the verification token entered by the user
     * @param paymentRequest the payment request details
     * @param user the authenticated user
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment(String token, PaymentRequestDTO paymentRequest, User user) {
        log.info("Processing payment for user {} with token {}", user.getLogin(), token);

        // Save the card details for future reference
        saveCardDetails(paymentRequest, user);

        try {
            // Send token to ACQ for verification
            String acqResponse = sendTokenToAcq(token);
            log.info("Received response from ACQ: {}", acqResponse);

            // Check if ACQ response contains ACK
            // Make sure the check is robust, e.g., ignore case or check for specific content
            boolean isVerified = acqResponse != null && acqResponse.contains("ACK");

            if (isVerified) {
                // Remplacer l'envoi RabbitMQ par un simple log
                // rabbitTemplate.convertAndSend(exchangeName, routingKey, "bonjour");
                log.info("RABBITMQ: Message 'bonjour' would be sent here");
                log.info("Payment successful for user {}", user.getLogin());
                return true;
            } else {
                log.warn("Payment verification failed for user {}. ACQ response: {}", user.getLogin(), acqResponse);
                return false;
            }
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send a verification token to ACQ
     * @param token the token to verify
     * @return the response from ACQ
     */
    private String sendTokenToAcq(String token) throws Exception {
        log.info("Sending token to ACQ for verification: {}", token);

        try (SSLSocket acqSocket = SslUtils.createSslClientSocket(
                acqPort,
                acqTruststorePath,
                acqTruststorePassword)) {
            PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));

            // Send the token to ACQ
            writer.println(token);
            log.info("Token sent to ACQ");

            // Receive the response from ACQ
            String response = reader.readLine();
            log.info("Received raw response from ACQ: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACQ: {}", e.getMessage(), e);
            throw e; // Re-throw exception to be handled by processPayment
        }
    }

    /**
     * Save or update card details
     * @param paymentRequest the payment request with card details
     * @param user the authenticated user
     * @return the saved card details
     */
    private CardDetails saveCardDetails(PaymentRequestDTO paymentRequest, User user) {
        // Utiliser la méthode renommée avec l'ID de l'utilisateur
        Optional<CardDetails> existingCard = cardDetailsRepository.findByCardNumberAndUserId(
                paymentRequest.getCardNumber(), user.getId()); // Utiliser user.getId()

        if (existingCard.isPresent()) {
            CardDetails card = existingCard.get();
            // Mettre à jour les champs transitoires si nécessaire (car ils ne sont pas persistés)
            card.setCardNumber(paymentRequest.getCardNumber());
            card.setExpirationDate(paymentRequest.getExpirationDate());
            card.setCvv(paymentRequest.getCvv());
            card.setCardholderName(paymentRequest.getCardholderName());
            // Mettre à jour la chaîne JSON si vous la conservez
            card.setCardDetails(String.format(
                    "{\"cardNumber\":\"%s\",\"expirationDate\":\"%s\",\"cardholderName\":\"%s\"}",
                    maskCardNumber(paymentRequest.getCardNumber()),
                    paymentRequest.getExpirationDate(),
                    paymentRequest.getCardholderName()
            ));
            log.info("Updating existing card details for user ID: {}", user.getId());
            return cardDetailsRepository.save(card);
        } else {
            CardDetails newCard = new CardDetails(
                    paymentRequest.getCardNumber(),
                    paymentRequest.getExpirationDate(),
                    paymentRequest.getCvv(),
                    paymentRequest.getCardholderName(),
                    user // Le constructeur prend l'objet User pour initialiser userId
            );
            // Assigner userId explicitement si le constructeur ne le fait pas ou si 'user' est null
            if (newCard.getUserId() == null && user != null) {
                newCard.setUserId(user.getId());
            }
            log.info("Saving new card details for user ID: {}", newCard.getUserId());
            return cardDetailsRepository.save(newCard);
        }
    }

    // Méthode pour masquer le numéro de carte (assurez-vous qu'elle est présente)
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            // Gérer les cas où le numéro de carte est trop court ou null
            return cardNumber;
        }
        // Masque tout sauf les 4 derniers chiffres
        return "XXXXXXXXXXXX" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Validate a credit card number using the Luhn algorithm
     * @param cardNumber the card number to validate
     * @return true if the card number is valid, false otherwise
     */
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

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
     * Check if a payment amount is within acceptable limits
     * @param amount the payment amount
     * @return true if the amount is acceptable, false otherwise
     */
    public boolean isAmountAcceptable(BigDecimal amount) {
        // Pour cet exemple, supposons que les montants inférieurs à 10000 sont valides
        // Vérifier si amount n'est pas null avant de comparer
        return amount != null && amount.compareTo(new BigDecimal("10000")) < 0;
    }
}