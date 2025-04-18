package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.CompletePaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.services.CardService;
import grapes.microservices.paymentbackend.services.ClientService;
import grapes.microservices.paymentbackend.services.PaymentService;
import grapes.microservices.paymentbackend.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller handling payment operations including initiation, verification, and completion.
 * Manages the payment flow using Transaction ID and Cache instead of Session for payment state.
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

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
     * Initiates a payment process by validating client authentication, card details,
     * and merging initial payment data. Creates a transaction record,
     * stores payment details in cache linked to transactionId, and initiates 3D Secure verification.
     *
     * @param paymentRequestFromForm The payment request containing card and amount details
     * @param session The HTTP session to retrieve client ID
     * @return ResponseEntity with success status, transactionId, or appropriate error message
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequestDTO paymentRequestFromForm, HttpSession session) {
        log.info("[PaymentController] /initiate endpoint called");

        // 1. Verify client authentication via session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Client not authenticated (session missing clientId)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        // 2. Get client from database
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.error("Client ID {} not found in DB despite being in session! Invalidating session.", clientId);
            session.invalidate(); // Invalidate session if client doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found. Please log in again."));
        }
        Client client = clientOpt.get();
        log.info("Client {} (ID {}) authenticated and retrieved.", client.getEmail(), client.getId());

        // 3. Merge initial data (from session) with form data
        PaymentRequestDTO finalPaymentRequest = paymentRequestFromForm;
        // Retrieve initial data if present (e.g., from external initiation via LoginController)
        BigDecimal initialAmount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String initialMerchant = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);

        if (initialAmount != null) {
            log.info("Using initial amount {} from session (overriding form amount if any: {})",
                    initialAmount, paymentRequestFromForm.getAmount());
            finalPaymentRequest.setAmount(initialAmount);
        } else if (finalPaymentRequest.getAmount() == null) {
            log.error("Missing payment amount from both initial session and form for client {}", client.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Payment amount is missing"));
        }

        if (initialMerchant != null) {
            finalPaymentRequest.setMerchantName(initialMerchant);
        } else if (finalPaymentRequest.getMerchantName() == null || finalPaymentRequest.getMerchantName().isEmpty()){
            finalPaymentRequest.setMerchantName("Grapes"); // Default merchant if none provided
        }
        log.info("Payment details: Amount={}, Merchant={}", finalPaymentRequest.getAmount(), finalPaymentRequest.getMerchantName());

        // Clean up initial session attributes now that they are merged/used
        session.removeAttribute(SESSION_INIT_AMOUNT_KEY);
        session.removeAttribute(SESSION_INIT_MERCHANT_KEY);
        session.removeAttribute(SESSION_INIT_PAYMENT_ID_KEY);
        log.debug("Cleared initial payment attributes from session.");

        // 4. Business validations (Balance, Card Ownership, Format)

        try {
            BigDecimal accountBalance = paymentService.getAccountBalance(client);
            if (accountBalance == null || accountBalance.compareTo(finalPaymentRequest.getAmount()) < 0) {
                log.warn("Insufficient balance for client {}. Balance: {}, Required: {}",
                        client.getEmail(), accountBalance, finalPaymentRequest.getAmount());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Insufficient account balance."));
            }
            log.info("Balance check passed for client {}.", client.getEmail());

            List<Card> clientCards = clientService.getClientCards(client.getId());
            boolean cardFound = false;
            for (Card card : clientCards) {
                if (card.getCardNumber().equals(finalPaymentRequest.getCardNumber())) {
                    cardFound = true;
                    if (!card.getExpirationDate().equals(finalPaymentRequest.getExpirationDate())) {
                        log.warn("Card expiration date mismatch. Provided: {}, Stored: {}",
                                finalPaymentRequest.getExpirationDate(), card.getExpirationDate());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("success", false, "message", "Card expiration date does not match our records."));
                    }
                    break;
                }
            }
            if (!cardFound) {
                log.warn("Card {} not found for client {}",
                        paymentService.maskCardNumber(finalPaymentRequest.getCardNumber()), client.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "The card number provided does not belong to your account."));
            }
            log.info("Card validation passed for client {}.", client.getEmail());

            // Format validations
            if (!paymentService.validateCardNumber(finalPaymentRequest.getCardNumber()) ||
                    !paymentService.isExpirationDateValid(finalPaymentRequest.getExpirationDate()) ||
                    !paymentService.isAmountAcceptable(finalPaymentRequest.getAmount())) {
                // Logs are already in paymentService methods
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Invalid card details or amount."));
            }
            log.info("Format validations passed for client {}.", client.getEmail());

        } catch (Exception validationEx) {
            log.error("Error during business validation for client {}: {}", client.getEmail(), validationEx.getMessage(), validationEx);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during validation."));
        }


        // 5. Create transaction and store payment details in cache
        TransactionEntity transaction = null; // Declare before try block
        Long transactionId = null;
        try {
            // Create transaction record FIRST to get the ID
            // Pass Card object if needed for TransactionService modification
            transaction = transactionService.createPaymentTransaction(finalPaymentRequest, client);
            transactionId = transaction.getId();
            log.info("Created transaction record with ID: {}", transactionId);

            // Store payment details in cache linked to transactionId
            Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
            if (pendingPaymentsCache != null) {
                pendingPaymentsCache.put(transactionId, finalPaymentRequest);
                log.info("Stored PaymentRequestDTO in cache for transactionId {}", transactionId);
            } else {
                log.error("Cache '{}' not found. Cannot store payment details.", PENDING_PAYMENTS_CACHE_NAME);
                // Cancel the created transaction?
                transactionService.failTransaction(transactionId, "Cache Error during Initiation");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
            }

            // 6. Initiate 3D Secure verification
            String otpCode = cardService.initiateCardVerification(
                    finalPaymentRequest, // Pass the DTO containing card details
                    client,
                    String.valueOf(transactionId) // Use transactionId as the unique context identifier
            );

            if (otpCode != null) {
                // Success - OTP sent (or simulated)
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Verification required. Check your phone for the OTP code.");
                response.put("transactionId", transactionId); // Return transactionId to frontend
                log.info("Verification initiated successfully for client {}, transaction ID: {}",
                        client.getEmail(), transactionId);
                return ResponseEntity.ok(response);
            } else {
                // Failed to initiate 3DS
                log.error("Failed to initiate card verification with ACS for transaction {}", transactionId);
                // Mark transaction as failed
                transactionService.failTransaction(transactionId, "ACS Initiation Failed");
                // Clean cache
                if (pendingPaymentsCache != null) {
                    pendingPaymentsCache.evictIfPresent(transactionId);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false,
                                "message", "Failed to initiate 3D Secure verification with the bank. Please try again later."));
            }
        } catch (Exception e) {
            log.error("Error during payment initiation processing for client {}: {}", client.getEmail(), e.getMessage(), e);
            // Clean cache and potentially fail transaction if initiation failed mid-way
            if (transactionId != null) {
                Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
                if (pendingPaymentsCache != null) {
                    pendingPaymentsCache.evictIfPresent(transactionId);
                }
                // Mark as failed if transaction was created but an error occurred afterwards
                if(transaction != null && !"Completed".equals(transaction.getStatus()) && !"Failed".equals(transaction.getStatus())) {
                    try {
                        transactionService.failTransaction(transactionId, "Initiation Exception");
                    } catch (Exception failEx) {
                        log.error("Also failed to mark transaction {} as failed after initiation error: {}", transactionId, failEx.getMessage());
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An unexpected error occurred during payment initiation."));
        }
    }

    /**
     * Completes the payment process after OTP verification using transactionId.
     * Retrieves pending payment details from cache, validates the provided token via ACQ,
     * and processes the final payment by updating the transaction status.
     *
     * @param completeRequest DTO containing the payment verification token (OTP) and transactionId
     * @param session The HTTP session to retrieve client ID
     * @return ResponseEntity with payment result or appropriate error message
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@Valid @RequestBody CompletePaymentRequestDTO completeRequest, HttpSession session) {
        Long transactionId = completeRequest.getTransactionId(); // Get transactionId from request
        log.info("Completing payment process for transaction ID: {}", transactionId);

        // 1. Get client ID from session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Client not authenticated for completing payment (transaction ID: {})", transactionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        // 2. Get client from database
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.warn("Client ID {} not found in DB during completion for transaction {}", clientId, transactionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found."));
        }
        Client client = clientOpt.get();

        // 3. Retrieve payment request details from cache
        PaymentRequestDTO paymentRequest = null;
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache != null) {
            try {

                Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
                if (wrapper != null) {
                    paymentRequest = (PaymentRequestDTO) wrapper.get();
                    log.info("Retrieved PaymentRequestDTO from cache for transactionId {}", transactionId);
                }
            } catch (Exception cacheEx) {
                log.error("Error retrieving data from cache for transactionId {}: {}", transactionId, cacheEx.getMessage(), cacheEx);
                // Consider this an internal error, maybe don't evict yet?
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Internal server error retrieving payment details."));
            }
        } else {
            log.error("Cache '{}' not found during completion for transactionId {}.", PENDING_PAYMENTS_CACHE_NAME, transactionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }


        if (paymentRequest == null) {
            log.warn("No pending payment request found in cache for transaction ID: {}. Maybe expired or already processed?", transactionId);
            // Check if already completed/failed in DB?
            Optional<TransactionEntity> existingTxOpt = transactionService.findTransactionById(transactionId); // Assuming TransactionService has findById
            if(existingTxOpt.isPresent()) {
                String status = existingTxOpt.get().getStatus();
                if ("Completed".equals(status) || "Failed".equals(status)) {
                    log.warn("Transaction {} already has status: {}", transactionId, status);
                    // Maybe return a message indicating it's already processed?
                    return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                            .body(Map.of("success", false, "message", "Payment request already processed or expired. Status: " + status));
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false,
                            "message", "Payment request not found or expired. Please restart the payment."));
        }

        // 4. Process payment with OTP verification via PaymentService
        boolean paymentSuccess = false;
        try {
            paymentSuccess = paymentService.processPayment(
                    completeRequest.getPaymentToken(),
                    paymentRequest,
                    client,
                    transactionId
            );

            // 5. Handle result and clean cache
            if (paymentSuccess) {
                log.info("Payment completed successfully for transaction ID: {}. Clearing cache.", transactionId);
                if (pendingPaymentsCache != null) {
                    pendingPaymentsCache.evict(transactionId); // Clean cache on success
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                response.put("transactionId", transactionId); // Confirm the ID processed
                return ResponseEntity.ok(response);
            } else {
                // OTP was incorrect or verification failed (PaymentService logs details)
                log.warn("Payment verification failed for transaction ID: {}", transactionId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false,
                                "message", "Verification failed. Please check the code and try again."));
            }
        } catch (Exception e) {
            // Includes potential exceptions from processPayment (e.g., DB error during completion)
            log.error("Error processing payment completion for transaction ID {}: {}",
                    transactionId, e.getMessage(), e);
            // Clean cache on internal error to prevent inconsistent state
            if (pendingPaymentsCache != null) {
                pendingPaymentsCache.evictIfPresent(transactionId);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "An internal error occurred during payment completion."));
        }
    }

    /**
     * Retrieves the details of a pending payment from the cache using transactionId.
     * Used to display payment confirmation information to the client on the OTP page.
     *
     * @param transactionId The ID of the transaction to retrieve details for.
     * @param session The HTTP session to retrieve client ID for authorization check.
     * @return ResponseEntity with payment details or appropriate error message.
     */
    @GetMapping("/pending-details")
    public ResponseEntity<?> getPendingPaymentDetails(@RequestParam @NotNull Long transactionId, HttpSession session) {
        log.info("Fetching pending payment details for transaction ID: {}", transactionId);

        // 1. Verify client authentication via session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Attempt to fetch pending details without active session for transactionId {}", transactionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired."));
        }

        // 2. Retrieve payment details from cache
        PaymentRequestDTO pendingPayment = null;
        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache != null) {
            try {
                Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
                if (wrapper != null) {
                    pendingPayment = (PaymentRequestDTO) wrapper.get();
                }
            } catch (Exception cacheEx) {
                log.error("Cache error fetching details for transactionId {}: {}", transactionId, cacheEx.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Error retrieving payment details (Cache)."));
            }
        } else {
            log.error("Cache '{}' not found fetching details for transactionId {}.", PENDING_PAYMENTS_CACHE_NAME, transactionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        if (pendingPayment == null) {
            log.warn("No pending payment found in cache for transaction ID {}", transactionId);
            // Optionally check DB status here too, like in /complete
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No pending payment found or it has expired."));
        }


        // 3. Fetch the transaction from DB to ensure the client in the session matches the transaction's client
        Optional<TransactionEntity> txOpt = transactionService.findTransactionById(transactionId);
        if(txOpt.isEmpty() || !txOpt.get().getClientId().equals(clientId)) {
            log.warn("Client ID {} in session does not match owner of transaction ID {} ({})",
                    clientId, transactionId, txOpt.map(TransactionEntity::getClientId).orElse(null));
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied to payment details."));
        }
        // Also check if transaction status is still 'Initiated'
        if (!"Initiated".equals(txOpt.get().getStatus())) {
            log.warn("Transaction {} is no longer pending (Status: {}).", transactionId, txOpt.get().getStatus());
            return ResponseEntity.status(HttpStatus.GONE) // 410 Gone
                    .body(Map.of("success", false, "message", "This payment is no longer pending verification."));
        }


        // 4. Format and return details
        try {
            String maskedCardNumber = paymentService.maskCardNumber(pendingPayment.getCardNumber());
            // Ensure consistent currency formatting (e.g., EUR with comma in French locale)
            String formattedAmount = String.format(Locale.FRANCE, "EUR %.2f", pendingPayment.getAmount());
            String merchantName = pendingPayment.getMerchantName() != null ?
                    pendingPayment.getMerchantName() : "Grapes"; // Default if somehow null

            Map<String, Object> details = new HashMap<>();
            details.put("success", true);
            details.put("merchantName", merchantName);
            details.put("amount", formattedAmount);
            details.put("maskedCardNumber", maskedCardNumber); // Use masked number

            log.info("Returning pending payment details for transactionId {}: Merchant={}, Amount={}, Card={}",
                    transactionId, merchantName, formattedAmount, maskedCardNumber);

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error formatting pending payment details for transactionId {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving payment details."));
        }
    }
}