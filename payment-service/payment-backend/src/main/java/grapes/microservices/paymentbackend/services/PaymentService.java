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

/**
 * Service responsible for payment processing operations including validation,
 * OTP verification via ACQ, client validation against OTP, and transaction completion.
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
    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;


    /**
     * Processes a payment after OTP verification.
     * 1. Verifies card ownership.
     * 2. Sends OTP and transaction context (transactionId) to ACQ/ACS for global validation.
     * 3. If ACK received (meaning OTP is valid AND matches the client for the transaction), completes the transaction.
     * 4. If NACK received, returns false allowing retry, does NOT fail the transaction immediately.
     *
     * @param token The OTP token submitted by the user
     * @param paymentRequest The original payment request details (retrieved from cache)
     * @param client The authenticated client making the payment (from session)
     * @param transactionId The specific ID of the transaction being processed
     * @return true if payment verification and processing succeeded, false otherwise
     */
    @Transactional
    public boolean processPayment(String token, PaymentRequestDTO paymentRequest, Client client, Long transactionId,Long orderId) {
        String context = "transactionId-" + transactionId + "-client-" + client.getId();
        log.info("Processing payment completion for client {} with token '{}' for context {}",
                client.getEmail(), maskToken(token), context);

        try {
            // 1. Verify card ownership
            verifyCardOwnership(paymentRequest.getCardNumber(), client.getId());

            // 2. Verify OTP token value AND context with ACQ/ACS
            String acqResponse = sendTokenAndContextToAcq(token, transactionId);

            // 3. Parse ACQ response
            log.info("Checking ACQ response for context {}. Raw response: '{}'", context, acqResponse);
            String expectedAckResponse = "Response from ACQ: ACK";
            boolean isVerifiedByAcs = acqResponse != null && acqResponse.trim().equals(expectedAckResponse);

            // 4. Process verification result
            if (isVerifiedByAcs) {
                // 4a. OTP and context are valid, proceed with transaction completion
                log.info("OTP token '{}' globally verified via ACQ/ACS for context {}", maskToken(token), context);

                // Complete the transaction (handles balance check, updates status)
                TransactionEntity completedTransaction = transactionService.completePaymentTransaction(
                        client,
                        paymentRequest.getAmount(),
                        transactionId,
                        orderId
                );
                log.info("Payment transaction ID {} completed successfully for client {} and context {}",
                        completedTransaction.getId(), client.getEmail(), context);
                return true; // Success

            } else {
                // 4b. OTP/Context invalid, expired, or ACQ/ACS issue.
                // DO NOT fail the transaction here. Allow user to retry.
                log.warn("Payment verification failed (ACQ/ACS NACK or invalid response) for client {}. Submitted OTP: '{}'. Context: {}. User can retry.",
                        client.getEmail(), maskToken(token), context);
                // REMOVED: transactionService.failTransaction(...) call is removed
                return false; // Indicate failure, allow retry
            }
            // Handle specific validation/state errors that occur *before* or *during* completion attempt
        } catch (IllegalArgumentException | SecurityException | IllegalStateException validationOrStateException) {
            log.error("Payment processing aborted due to validation or state issue for context {}: {}",
                    context, validationOrStateException.getMessage());
            // Fail transaction on critical validation/state errors (e.g., insufficient balance detected during completion attempt)
            try {
                // Check if transaction still exists and is in 'Initiated' state before failing
                Optional<TransactionEntity> txOpt = transactionService.findTransactionById(transactionId);
                if (txOpt.isPresent() && "Initiated".equals(txOpt.get().getStatus())) {
                    transactionService.failTransaction(transactionId, "Validation or State Error: " + validationOrStateException.getMessage());
                } else {
                    log.warn("Transaction {} not failed after validation/state error because it was not found or not in 'Initiated' state.", transactionId);
                }
            } catch (Exception failEx) {
                log.error("Also failed to mark transaction {} as failed after validation/state error: {}", transactionId, failEx.getMessage());
            }
            return false;
            // Handle unexpected errors during the process
        } catch (Exception e) {
            log.error("Unexpected error during payment completion for context {}: {}",
                    context, e.getMessage(), e);
            // Fail transaction on unexpected errors if it's still pending
            try {
                Optional<TransactionEntity> txOpt = transactionService.findTransactionById(transactionId);
                if (txOpt.isPresent() && "Initiated".equals(txOpt.get().getStatus())) {
                    transactionService.failTransaction(transactionId, "Unexpected Processing Exception");
                } else {
                    log.warn("Transaction {} not failed after unexpected error because it was not found or not in 'Initiated' state.", transactionId);
                }
            } catch (Exception failEx) {
                log.error("Also failed to mark transaction {} as failed after unexpected error: {}", transactionId, failEx.getMessage());
            }
            return false;
        }
    }

    /**
     * Verifies that a card belongs to the specified client.
     * (Private helper method)
     */
    private void verifyCardOwnership(String cardNumber, Long clientId) {
        Optional<Card> cardOpt = cardRepository.findByCardNumberAndClientId(cardNumber, clientId);
        if (cardOpt.isEmpty()) {
            String maskedCard = maskCardNumber(cardNumber); // Use masking method
            log.error("Card ending in {} does not belong to client {}", maskedCard.substring(maskedCard.length() - 4), clientId);
            throw new SecurityException("Card does not belong to this client");
        }
        // log.debug("Card ownership verified for client {}", clientId);
    }

    /**
     * Sends an OTP token AND transaction context to the ACQ server for verification.
     * (Private helper method)
     */
    private String sendTokenAndContextToAcq(String token, Long transactionId) {
        String dataToSend = token + "#" + transactionId; // Simple format: token#txId
        log.debug("Sending token {} and txId {} to ACQ on port {} using truststore {}",
                maskToken(token), transactionId, acqPort, clientTruststorePath);
        try (SSLSocket acqSocket = SslUtils.createSslClientSocket(
                acqPort,
                clientTruststorePath,      // Client's truststore to trust ACQ
                clientTruststorePassword)) {
            PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));

            writer.println(dataToSend); // Send combined token and txId
            log.debug("Token and context sent to ACQ.");

            String response = reader.readLine(); // Read raw response
            log.info("Raw response received from ACQ: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACQ on port {}: {}", acqPort, e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with ACQ verification server.", e);
        }
    }

    /**
     * Masks a card number for display or logging purposes.
     * (Public utility method)
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        int length = cardNumber.length();
        return "************" + cardNumber.substring(length - 4);
    }

    /**
     * Masks an OTP token for secure logging.
     * (Private helper method)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 2) {
            return "****";
        }
        return token.charAt(0) + "*".repeat(Math.max(0, token.length() - 2)) + token.charAt(token.length() - 1);
    }

    /**
     * Validates a card number format using the Luhn algorithm.
     * (Public utility method)
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
        return (sum % 10 == 0);
    }

    /**
     * Verifies if a card expiration date is valid (format MM/YYYY) and not expired.
     * (Public utility method)
     */
    public boolean isExpirationDateValid(String expirationDateStr) {
        // Regex updated for MM/YYYY and years 2024 onwards
        if (expirationDateStr == null || !expirationDateStr.matches("^(0[1-9]|1[0-2])\\/20(2[4-9]|[3-9]\\d)$")) {
            log.warn("Invalid expiration date format received (expecting MM/YYYY, >= 2024): {}", expirationDateStr);
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth expirationYearMonth = YearMonth.parse(expirationDateStr, formatter);
            YearMonth currentYearMonth = YearMonth.now(ZoneId.systemDefault());
            // Card is valid if expiration month/year is not before the current month/year
            return !expirationYearMonth.isBefore(currentYearMonth);
        } catch (DateTimeParseException e) {
            log.error("Error parsing expiration date: {}", expirationDateStr, e);
            return false;
        }
    }

    /**
     * Verifies if a payment amount is acceptable (positive and within limits).
     * (Public utility method)
     */
    public boolean isAmountAcceptable(BigDecimal amount) {
        if (amount == null) return false;
        // Example: Amount must be > 0 and < 10000
        return amount.compareTo(BigDecimal.ZERO) > 0
                && amount.compareTo(new BigDecimal("10000")) < 0;
    }

    /**
     * Gets the account balance for a client by finding their most recent account.
     * (Public utility method)
     */
    public BigDecimal getAccountBalance(Client client) {
        // Find the most recently opened account for the client
        Optional<Account> accountOpt = accountRepository.findFirstByClientIdOrderByOpeningDateDesc(client.getId());
        if (accountOpt.isEmpty()) {
            log.error("No account found for client ID: {}", client.getId());
            // Throwing exception as balance check is usually critical
            throw new IllegalStateException("Client has no associated account");
        }
        Account account = accountOpt.get();
        // Return Zero if balance is null to prevent NullPointerExceptions later
        return account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
    }
}