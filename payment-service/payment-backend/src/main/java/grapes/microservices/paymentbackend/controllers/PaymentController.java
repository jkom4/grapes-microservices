package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.TokenDTO;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.services.CardService;
import grapes.microservices.paymentbackend.services.ClientService;
import grapes.microservices.paymentbackend.services.PaymentService;
import grapes.microservices.paymentbackend.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller handling payment operations including initiation, verification, and completion.
 * Manages the payment flow from user form submission through verification to final processing.
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

    // Session keys
    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_PENDING_PAYMENT_KEY = "pendingPaymentRequest";
    private static final String SESSION_CURRENT_ATTEMPT_ID_KEY = "currentPaymentAttemptId";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";

    /**
     * Initiates a payment process by validating client authentication, card details,
     * and merging initial payment data from session with form data.
     * Creates a transaction record and initiates 3D Secure verification.
     *
     * @param paymentRequestFromForm The payment request containing card and amount details
     * @param session The HTTP session to retrieve client ID and store payment data
     * @return ResponseEntity with success status or appropriate error message
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
            session.invalidate();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found. Please log in again."));
        }

        Client client = clientOpt.get();
        log.info("Client {} (ID {}) authenticated and retrieved.", client.getEmail(), client.getId());

        // 3. Merge initial data (from session) with form data
        PaymentRequestDTO finalPaymentRequest = paymentRequestFromForm;
        BigDecimal initialAmount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String initialMerchant = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);
        String initialPaymentId = (String) session.getAttribute(SESSION_INIT_PAYMENT_ID_KEY);

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
        } else {
            finalPaymentRequest.setMerchantName("Grapes"); // Default
        }

        log.info("Payment details: Amount={}, Merchant={}", finalPaymentRequest.getAmount(), finalPaymentRequest.getMerchantName());

        // Generate or retrieve attempt ID
        String paymentAttemptId = (initialPaymentId != null) ? initialPaymentId : UUID.randomUUID().toString();
        session.setAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY, paymentAttemptId);
        log.info("Using Payment Attempt ID: {}", paymentAttemptId);

        // Clean up initial session attributes
        session.removeAttribute(SESSION_INIT_AMOUNT_KEY);
        session.removeAttribute(SESSION_INIT_MERCHANT_KEY);
        session.removeAttribute(SESSION_INIT_PAYMENT_ID_KEY);
        log.debug("Cleared initial payment attributes from session.");

        // 4. Business validations
        // Check account balance
        BigDecimal accountBalance = null;
        try {
            accountBalance = paymentService.getAccountBalance(client);
        } catch (Exception e) {
            log.error("Error retrieving account balance for client {}: {}", client.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving account balance."));
        }

        if (accountBalance == null || accountBalance.compareTo(finalPaymentRequest.getAmount()) < 0) {
            log.warn("Insufficient balance for client {}. Balance: {}, Required: {}",
                    client.getEmail(), accountBalance, finalPaymentRequest.getAmount());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Insufficient account balance."));
        }
        log.info("Balance check passed for client {}.", client.getEmail());

        // Verify card ownership
        List<Card> clientCards = clientService.getClientCards(client.getId());
        boolean cardFound = false;
        for (Card card : clientCards) {
            if (card.getCardNumber().equals(finalPaymentRequest.getCardNumber())) {
                cardFound = true;

                // Check expiration date
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

        // 5. Format validations
        if (!paymentService.validateCardNumber(finalPaymentRequest.getCardNumber())) {
            log.warn("Invalid card number format or Luhn check failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid card number format."));
        }

        if (!paymentService.isExpirationDateValid(finalPaymentRequest.getExpirationDate())) {
            log.warn("Card expiration date is invalid or expired: {}", finalPaymentRequest.getExpirationDate());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Card expiration date is invalid or expired."));
        }

        if (!paymentService.isAmountAcceptable(finalPaymentRequest.getAmount())) {
            log.warn("Payment amount exceeds limit or is invalid: {}", finalPaymentRequest.getAmount());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Payment amount is invalid or exceeds limit."));
        }
        log.info("Format validations passed for client {}.", client.getEmail());

        // 6. Store payment request in session and create transaction
        try {
            // Store in session
            session.setAttribute(SESSION_PENDING_PAYMENT_KEY, finalPaymentRequest);
            log.info("Stored final payment request in session for client {}", client.getEmail());

            // Create transaction record
            TransactionEntity transaction = transactionService.createPaymentTransaction(finalPaymentRequest, client);
            log.info("Created transaction record with ID: {}", transaction.getId());

            // Initiate 3D Secure verification
            String otpCode = cardService.initiateCardVerification(
                    finalPaymentRequest,
                    client,
                    paymentAttemptId
            );

            if (otpCode != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Verification required. Check your phone for the OTP code.");
                response.put("transactionId", transaction.getId());
                log.info("Verification initiated successfully for client {}, payment attempt ID: {}",
                        client.getEmail(), paymentAttemptId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to initiate card verification with ACS for client {}", client.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false,
                                "message", "Failed to initiate 3D Secure verification with the bank. Please try again later."));
            }
        } catch (Exception e) {
            log.error("Error during payment initiation for client {}: {}", client.getEmail(), e.getMessage(), e);
            session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
            session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An unexpected error occurred during payment initiation."));
        }
    }

    /**
     * Completes the payment process after OTP verification.
     * Retrieves the pending payment from session, validates the provided token,
     * and processes the final payment.
     *
     * @param tokenDTO The DTO containing the payment verification token
     * @param session The HTTP session to retrieve client ID and pending payment data
     * @return ResponseEntity with payment result or appropriate error message
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody TokenDTO tokenDTO, HttpSession session) {
        log.info("Completing payment process with verification code");

        // Get client ID from session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Client not authenticated for completing payment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        // Get client from database
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.warn("Client ID {} not found in DB during completion", clientId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found."));
        }
        Client client = clientOpt.get();

        // Get pending payment request from session
        PaymentRequestDTO paymentRequest = (PaymentRequestDTO) session.getAttribute(SESSION_PENDING_PAYMENT_KEY);
        if (paymentRequest == null) {
            log.warn("No pending payment request found in session for client {}", client.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false,
                            "message", "No pending payment request found or session expired. Please restart the payment."));
        }

        // Get payment attempt ID
        String paymentAttemptId = (String) session.getAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
        if (paymentAttemptId == null) {
            paymentAttemptId = "UNKNOWN-" + UUID.randomUUID().toString();
            log.warn("Payment Attempt ID not found in session for client {}. Using fallback: {}",
                    client.getEmail(), paymentAttemptId);
        }

        try {
            // Process payment with OTP verification
            boolean paymentSuccess = paymentService.processPayment(
                    tokenDTO.getPaymentToken(),
                    paymentRequest,
                    client
            );

            if (paymentSuccess) {
                log.info("Payment completed successfully for client {}, attempt ID: {}. Clearing session attributes.",
                        client.getEmail(), paymentAttemptId);
                session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
                session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                return ResponseEntity.ok(response);
            } else {
                // OTP was incorrect or verification failed
                log.warn("Payment verification failed for client {}, attempt ID: {}",
                        client.getEmail(), paymentAttemptId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false,
                                "message", "Verification failed. Please check the code and try again."));
            }
        } catch (Exception e) {
            log.error("Error processing payment completion for client {}: {}",
                    client.getEmail(), e.getMessage(), e);
            session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
            session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "An internal error occurred during payment completion."));
        }
    }

    /**
     * Retrieves the details of a pending payment from the session.
     * Used to display payment confirmation information to the client.
     *
     * @param session The HTTP session to retrieve pending payment data
     * @return ResponseEntity with payment details or appropriate error message
     */
    @GetMapping("/pending-details")
    public ResponseEntity<?> getPendingPaymentDetails(HttpSession session) {
        log.info("Fetching pending payment details");

        // Get client ID from session
        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Attempt to fetch pending details without active session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired."));
        }

        // Get pending payment from session
        PaymentRequestDTO pendingPayment = (PaymentRequestDTO) session.getAttribute(SESSION_PENDING_PAYMENT_KEY);
        if (pendingPayment == null) {
            log.warn("No pending payment found in session for client ID {}", clientId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No pending payment found."));
        }

        try {
            // Format data for response
            String maskedCardNumber = paymentService.maskCardNumber(pendingPayment.getCardNumber());
            // Change from "EURO" (French) to "EUR" (international currency code)
            String formattedAmount = String.format(Locale.US, "EUR %.2f", pendingPayment.getAmount());
            String merchantName = pendingPayment.getMerchantName() != null ?
                    pendingPayment.getMerchantName() : "Grapes";

            Map<String, Object> details = new HashMap<>();
            details.put("success", true);
            details.put("merchantName", merchantName);
            details.put("amount", formattedAmount);
            details.put("maskedCardNumber", maskedCardNumber);

            log.info("Returning pending payment details: Merchant={}, Amount={}, Card={}",
                    merchantName, formattedAmount, maskedCardNumber);

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error retrieving pending payment details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving payment details."));
        }
    }
}