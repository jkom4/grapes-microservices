package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
import grapes.microservices.paymentbackend.utils.SslUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for payment processing operations including validation,
 * OTP verification, and transaction completion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    // ACQ communication configuration
    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.truststore.acq.path}")
    private String acqTruststorePath;

    @Value("${app.truststore.acq.password}")
    private String acqTruststorePassword;


    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;

    /**
     * Processes a payment after OTP verification by communicating with the ACQ server
     * to verify the token, then completing the transaction if successful.
     *
     * @param token The OTP token submitted by the user
     * @param paymentRequest The original payment request containing card details
     * @param client The authenticated client making the payment
     * @return true if payment verification and processing succeeded, false otherwise
     */
    @Transactional
    public boolean processPayment(String token, PaymentRequestDTO paymentRequest, Client client) {
        String transactionContextId = "paymentAttempt-" + UUID.randomUUID().toString();
        log.info("Processing payment completion for client {} with token '{}' for context {}",
                client.getEmail(), maskToken(token), transactionContextId);

        try {
            // 1. Verify card ownership
            verifyCardOwnership(paymentRequest.getCardNumber(), client.getId());

            // 2. Verify OTP token with ACQ server
            String acqResponse = sendTokenToAcq(token);

            // 3. Parse ACQ response
            log.info("Checking ACQ response for context {}. Raw response: '{}'",
                    transactionContextId, acqResponse);

            String expectedAckResponse = "Response from ACQ: ACK";
            boolean isVerified = false;

            if (acqResponse != null) {
                // Exact case-sensitive comparison
                isVerified = acqResponse.trim().equals(expectedAckResponse);
                log.info("Does the response ('{}') exactly match the expected ACK ('{}')? {} (Context: {})",
                        acqResponse.trim(), expectedAckResponse, isVerified, transactionContextId);
            } else {
                log.warn("Received null response from ACQ. Verification failed. (Context: {})", transactionContextId);
            }

            // 4. Process verification result
            if (isVerified) {
                // Token is valid - proceed with account debit and finalization
                log.info("OTP token '{}' successfully verified via ACQ for client {} and context {}",
                        maskToken(token), client.getEmail(), transactionContextId);

                // 5. Complete the transaction (debit account, update transaction status)
                TransactionEntity completedTransaction = transactionService.completePaymentTransaction(
                        client,
                        paymentRequest.getAmount(),
                        transactionContextId
                );

                log.info("Payment transaction ID {} completed successfully for client {} and context {}",
                        completedTransaction.getId(), client.getEmail(), transactionContextId);
                return true;

            } else {
                // Token is invalid or ACQ returned error/NACK
                log.warn("Payment verification failed for client {}. Submitted OTP: '{}'. ACQ raw response: '{}'. Context: {}",
                        client.getEmail(), maskToken(token), acqResponse, transactionContextId);
                return false;
            }
        } catch (IllegalArgumentException cardEx) {
            // Card ownership verification failure
            log.error("Payment processing aborted due to card ownership verification failure for context {}: {}",
                    transactionContextId, cardEx.getMessage());
            return false;
        } catch (IllegalStateException balanceEx) {
            // Insufficient balance error during transaction completion
            log.error("Payment processing aborted due to insufficient balance for context {}: {}",
                    transactionContextId, balanceEx.getMessage());
            return false;
        } catch (Exception e) {
            // Other errors (ACQ communication, database, etc.)
            log.error("Unexpected error during payment completion for context {}: {}",
                    transactionContextId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifies that a card belongs to the specified client.
     *
     * @param cardNumber The card number to verify
     * @param clientId The client ID who should own the card
     * @throws IllegalArgumentException if the card doesn't belong to the client
     */
    private void verifyCardOwnership(String cardNumber, Long clientId) {
        Optional<Card> cardOpt = cardRepository.findByCardNumberAndClientId(cardNumber, clientId);
        if (cardOpt.isEmpty()) {
            log.error("Card {} does not belong to client {}", maskCardNumber(cardNumber), clientId);
            throw new IllegalArgumentException("Card does not belong to this client");
        }
        log.debug("Card ownership verified for card ending in {} and client {}",
                cardNumber.substring(cardNumber.length() - 4), clientId);
    }

    /**
     * Sends an OTP token to the ACQ server for verification.
     *
     * @param token The OTP token to verify
     * @return The ACQ server response
     * @throws Exception if communication with ACQ fails
     */
    private String sendTokenToAcq(String token) throws Exception {
        log.info("Sending token to ACQ on port {} for verification: {}", acqPort, maskToken(token));
        try (SSLSocket acqSocket = SslUtils.createSslClientSocket(
                acqPort,
                clientTruststorePath,
                clientTruststorePassword)) {
            PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));

            // Send only the token as expected by AcqServer.handleClientRequest
            writer.println(token);
            log.info("Token sent to ACQ.");

            // Read the response
            String response = reader.readLine();
            log.info("Received raw response from ACQ: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACQ on port {}: {}", acqPort, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Masks a card number for display or logging purposes, showing only last 4 digits.
     *
     * @param cardNumber The card number to mask
     * @return The masked card number
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        int length = cardNumber.length();
        return "X".repeat(length - 4) + cardNumber.substring(length - 4);
    }

    /**
     * Masks an OTP token for secure logging, showing only first and last digits.
     *
     * @param token The token to mask
     * @return The masked token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 2) {
            return "****";
        }
        // Show first and last digit, mask the rest
        return token.charAt(0) + "*".repeat(Math.max(0, token.length() - 2)) + token.charAt(token.length() - 1);
    }

    /**
     * Validates a card number format using the Luhn algorithm.
     *
     * @param cardNumber The card number to validate
     * @return true if the card number is valid, false otherwise
     */
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String cleanedCardNumber = cardNumber.replaceAll("\\s+", ""); // Remove spaces
        if (cleanedCardNumber.length() < 13 || cleanedCardNumber.length() > 19 || !cleanedCardNumber.matches("\\d+")) {
            log.warn("Invalid card number format or length: {}", maskCardNumber(cardNumber));
            return false;
        }

        // Luhn algorithm
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
        boolean isValid = (sum % 10 == 0);
        log.debug("Luhn validation for card ending in {}: {}", cleanedCardNumber.substring(cleanedCardNumber.length()-4), isValid);
        return isValid;
    }

    /**
     * Verifies if a payment amount is acceptable (positive and within limits).
     *
     * @param amount The payment amount to verify
     * @return true if the amount is acceptable, false otherwise
     */
    public boolean isAmountAcceptable(BigDecimal amount) {
        if (amount == null) return false;
        boolean acceptable = amount.compareTo(BigDecimal.ZERO) > 0 // Strictly positive
                && amount.compareTo(new BigDecimal("10000")) < 0; // Less than 10000 (arbitrary limit)
        if (!acceptable) {
            log.warn("Payment amount {} is not acceptable (must be > 0 and < 10000).", amount);
        }
        return acceptable;
    }

    /**
     * Verifies if a card expiration date is valid and not expired.
     * Expected format: MM/YYYY
     *
     * @param expirationDateStr The expiration date string to verify
     * @return true if the date is valid and not expired, false otherwise
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
            // Card is valid if expiration month/year is >= current month/year
            boolean isValid = !expirationYearMonth.isBefore(currentYearMonth);
            if (!isValid) {
                log.warn("Expiration date {} is in the past.", expirationDateStr);
            }
            return isValid;
        } catch (DateTimeParseException e) {
            log.error("Error parsing expiration date: {}", expirationDateStr, e);
            return false;
        }
    }

    /**
     * Gets the account balance for a client by finding their most recent account.
     *
     * @param client The client whose balance to retrieve
     * @return The account balance
     * @throws IllegalStateException if the client has no account or balance is null
     */
    public BigDecimal getAccountBalance(Client client) {
        Optional<Account> accountOpt = accountRepository.findFirstByClientIdOrderByOpeningDateDesc(client.getId());
        if (accountOpt.isEmpty()) {
            log.error("No account found for client ID: {}", client.getId());
            throw new IllegalStateException("Client has no associated account");
        }
        Account account = accountOpt.get();
        if (account.getBalance() == null) {
            log.warn("Account {} for client {} has a null balance.", account.getAccountNumber(), client.getId());
            return BigDecimal.ZERO;
        }
        return account.getBalance();
    }
}