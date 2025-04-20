package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.CompletePaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service handling payment processing, validations, and state management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final PaymentService paymentService;
    private final CardService cardService;
    private final ClientService clientService;
    private final TransactionService transactionService;
    private final CacheManager cacheManager;

    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";

    private static final String PENDING_PAYMENTS_CACHE_NAME = "pendingPayments";

    /**
     * Initiates a payment after validating client, card details and creating a transaction.
     *
     * @param paymentRequest The payment request containing card and amount details
     * @param session The HTTP session containing client ID
     * @return ResponseEntity with the result
     */
    public ResponseEntity<?> initiatePayment(PaymentRequestDTO paymentRequest, HttpSession session) {
        log.info("[PaymentProcessingService] Initiating payment");

        // Verify client authentication via session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Client not authenticated (session missing clientId)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        // Get client from database
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.error("Client ID {} not found in DB despite being in session! Invalidating session.", clientId);
            session.invalidate();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found. Please log in again."));
        }

        Client client = clientOpt.get();

        // Prepare final payment request by merging with session data if needed
        PaymentRequestDTO finalPaymentRequest = prepareFinalPaymentRequest(paymentRequest, session);

        // Validate payment request
        ResponseEntity<?> validationResult = validatePaymentRequest(finalPaymentRequest, client);
        if (validationResult != null) {
            return validationResult;
        }

        // Create transaction and store payment details in cache
        TransactionEntity transaction = transactionService.createPaymentTransaction(finalPaymentRequest, client);
        Long transactionId = transaction.getId();

        // Store payment request in cache
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            transactionService.failTransaction(transactionId, "Cache Error during Initiation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        pendingPaymentsCache.put(transactionId, finalPaymentRequest);

        // Initiate card verification
        String otpCode = cardService.initiateCardVerification(finalPaymentRequest, client, String.valueOf(transactionId));
        if (otpCode == null) {
            // Failed to initiate 3DS
            transactionService.failTransaction(transactionId, "ACS Initiation Failed");
            pendingPaymentsCache.evictIfPresent(transactionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message",
                            "Failed to initiate 3D Secure verification with the bank. Please try again later."));
        }

        // Success - OTP sent
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Verification required. Check your phone for the OTP code.");
        response.put("transactionId", transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Prepares the final payment request by merging form data with session data if present.
     *
     * @param paymentRequest The original payment request
     * @param session The HTTP session
     * @return The merged payment request
     */
    private PaymentRequestDTO prepareFinalPaymentRequest(PaymentRequestDTO paymentRequest, HttpSession session) {
        PaymentRequestDTO finalRequest = paymentRequest;

        // Retrieve initial data if present
        BigDecimal initialAmount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String initialMerchant = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);

        if (initialAmount != null) {
            finalRequest.setAmount(initialAmount);
        }

        if (initialMerchant != null) {
            finalRequest.setMerchantName(initialMerchant);
        } else if (finalRequest.getMerchantName() == null || finalRequest.getMerchantName().isEmpty()) {
            finalRequest.setMerchantName("Grapes"); // Default merchant if none provided
        }

        // Clean up initial session attributes
        session.removeAttribute(SESSION_INIT_AMOUNT_KEY);
        session.removeAttribute(SESSION_INIT_MERCHANT_KEY);
        session.removeAttribute(SESSION_INIT_PAYMENT_ID_KEY);

        return finalRequest;
    }

    /**
     * Validates the payment request against business rules with detailed error messages.
     *
     * @param request The payment request to validate
     * @param client The client making the payment
     * @return ResponseEntity with error details if validation fails, null if valid
     */
    private ResponseEntity<?> validatePaymentRequest(PaymentRequestDTO request, Client client) {
        try {
            // 1. Validate card format first
            if (!paymentService.validateCardNumber(request.getCardNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Invalid card number format."));
            }

            // 2. Validate expiration date
            if (!paymentService.isExpirationDateValid(request.getExpirationDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Card has expired or expiration date format is invalid."));
            }

            // 3. Verify card ownership
            List<Card> clientCards = clientService.getClientCards(client.getId());
            boolean cardFound = false;
            for (Card card : clientCards) {
                if (card.getCardNumber().equals(request.getCardNumber())) {
                    cardFound = true;

                    // 4. Check if expiration date matches records
                    if (!card.getExpirationDate().equals(request.getExpirationDate())) {
                        log.warn("Card expiration date mismatch. Provided: {}, Stored: {}",
                                request.getExpirationDate(), card.getExpirationDate());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("success", false, "message", "Card expiration date doesn't match our records."));
                    }
                    break;
                }
            }

            if (!cardFound) {
                log.warn("Card {} not found for client {}",
                        paymentService.maskCardNumber(request.getCardNumber()), client.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "This card is not associated with your account."));
            }

            // 5. Check account balance
            BigDecimal accountBalance = paymentService.getAccountBalance(client);
            if (accountBalance == null || accountBalance.compareTo(request.getAmount()) < 0) {
                log.warn("Insufficient balance for client {}. Balance: {}, Required: {}",
                        client.getEmail(), accountBalance, request.getAmount());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Insufficient funds in your account to complete this transaction."));
            }

            // 6. Validate amount (we keep this validation but with a unique message)
            if (!paymentService.isAmountAcceptable(request.getAmount())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Payment amount is invalid or exceeds transaction limits."));
            }

            // All validations passed
            return null;

        } catch (Exception e) {
            log.error("Error during payment validation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "A technical error occurred during payment validation."));
        }
    }

    /**
     * Completes a payment after OTP verification.
     *
     * @param completeRequest The payment completion request with OTP
     * @param session The HTTP session
     * @return ResponseEntity with the result
     */
    public ResponseEntity<?> completePayment(CompletePaymentRequestDTO completeRequest, HttpSession session) {
        Long transactionId = completeRequest.getTransactionId();
        log.info("Completing payment for transaction ID: {}", transactionId);

        // Verify client session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        // Get client from database
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found."));
        }

        Client client = clientOpt.get();

        // Retrieve payment request from cache
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
        if (wrapper == null) {
            return checkTransactionStatus(transactionId);
        }

        PaymentRequestDTO paymentRequest = (PaymentRequestDTO) wrapper.get();

        // Process payment with OTP verification
        boolean paymentSuccess = paymentService.processPayment(
                completeRequest.getPaymentToken(),
                paymentRequest,
                client,
                transactionId
        );

        if (paymentSuccess) {
            pendingPaymentsCache.evict(transactionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "transactionId", transactionId
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Verification failed. The code entered is incorrect or has expired."));
        }
    }

    /**
     * Checks the status of a transaction that's not in the cache.
     *
     * @param transactionId The transaction ID to check
     * @return ResponseEntity with appropriate status information
     */
    private ResponseEntity<?> checkTransactionStatus(Long transactionId) {
        Optional<TransactionEntity> existingTxOpt = transactionService.findTransactionById(transactionId);
        if (existingTxOpt.isPresent()) {
            String status = existingTxOpt.get().getStatus();
            if ("Completed".equals(status)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false,
                                "message", "This payment has already been processed successfully."));
            } else if ("Failed".equals(status)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false,
                                "message", "This payment request has already failed processing."));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Payment request not found or expired. Please restart the payment process."));
    }

    /**
     * Retrieves pending payment details.
     *
     * @param transactionId The transaction ID
     * @param session The HTTP session
     * @return ResponseEntity with payment details or error
     */
    public ResponseEntity<?> getPendingPaymentDetails(Long transactionId, HttpSession session) {
        log.info("Fetching pending payment details for transaction ID: {}", transactionId);

        // Verify client authentication via session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired."));
        }

        // Retrieve payment details from cache
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
        if (wrapper == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No pending payment found or it has expired."));
        }

        PaymentRequestDTO pendingPayment = (PaymentRequestDTO) wrapper.get();

        // Verify transaction ownership and status
        Optional<TransactionEntity> txOpt = transactionService.findTransactionById(transactionId);
        if (txOpt.isEmpty() || !txOpt.get().getClientId().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied to payment details."));
        }

        if (!"Initiated".equals(txOpt.get().getStatus())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("success", false, "message", "This payment is no longer pending verification."));
        }

        // Format and return details
        String maskedCardNumber = paymentService.maskCardNumber(pendingPayment.getCardNumber());
        String formattedAmount = String.format(Locale.FRANCE, "EUR %.2f", pendingPayment.getAmount());
        String merchantName = pendingPayment.getMerchantName() != null ? pendingPayment.getMerchantName() : "Grapes";

        return ResponseEntity.ok(Map.of(
                "success", true,
                "merchantName", merchantName,
                "amount", formattedAmount,
                "maskedCardNumber", maskedCardNumber
        ));
    }

    /**
     * Handles errors during payment initiation.
     *
     * @param e The exception that occurred
     * @return ResponseEntity with appropriate error message
     */
    public ResponseEntity<?> handleInitiationError(Exception e) {
        log.error("Error during payment initiation: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An unexpected error occurred during payment initiation."));
    }

    /**
     * Retrieves payment details from the session.
     *
     * @param session The HTTP session containing payment details
     * @return ResponseEntity with payment details or error message
     */
    public ResponseEntity<?> getSessionDetails(HttpSession session) {
        BigDecimal amount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String merchantName = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);

        if (amount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No payment details found in session"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "amount", amount,
                "merchantName", merchantName != null ? merchantName : "Grapes"
        ));
    }

    /**
     * Handles errors during payment completion.
     *
     * @param e The exception that occurred
     * @param transactionId The transaction ID being processed
     * @return ResponseEntity with appropriate error message
     */
    public ResponseEntity<?> handleCompletionError(Exception e, Long transactionId) {
        log.error("Error during payment completion for transaction ID {}: {}", transactionId, e.getMessage(), e);

        // Clean cache on error
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache != null) {
            pendingPaymentsCache.evictIfPresent(transactionId);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An internal error occurred during payment completion."));
    }

    /**
     * Handles errors when retrieving pending payment details.
     *
     * @param e The exception that occurred
     * @return ResponseEntity with appropriate error message
     */
    public ResponseEntity<?> handlePendingDetailsError(Exception e) {
        log.error("Error retrieving pending payment details: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving payment details."));
    }
}