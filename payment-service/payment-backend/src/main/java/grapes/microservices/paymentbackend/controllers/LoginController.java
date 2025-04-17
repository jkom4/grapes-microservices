package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.services.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller handling user authentication and payment initiation operations.
 * Provides endpoints for login, payment initiation, and redirects.
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final ClientService clientService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // Session attribute keys
    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";
    private static final String SESSION_CLIENT_EMAIL_KEY = "clientEmail";

    /**
     * Initiates a payment process by capturing payment details and storing them in the session.
     * Generates a unique payment ID and prepares a redirect URL to the login page.
     *
     * @param paymentRequest The payment request containing amount and merchant identifier
     * @param request The HTTP request to access and modify the session
     * @return ResponseEntity containing success/error status and redirect URL
     */
    @PostMapping("/api/login/payment-initiate")
    @ResponseBody
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateRequest paymentRequest, HttpServletRequest request) {
        try {
            BigDecimal amount = paymentRequest.getAmount();
            String merchantId = paymentRequest.getMerchantId();

            if (amount == null || merchantId == null || merchantId.isEmpty()) {
                log.warn("[LoginController] Missing amount or merchantId in payment initiation request");
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Amount and merchantId (merchant identifier) are required"
                ));
            }

            // Generate ID for this initiation attempt
            String initialPaymentId = UUID.randomUUID().toString();
            log.info("[LoginController] Payment initiated via external request - Initial ID: {}, Amount: {}, Merchant: {}",
                    initialPaymentId, amount, merchantId);

            // Store in session
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_INIT_AMOUNT_KEY, amount);
            session.setAttribute(SESSION_INIT_MERCHANT_KEY, merchantId);
            session.setAttribute(SESSION_INIT_PAYMENT_ID_KEY, initialPaymentId);
            log.info("[LoginController] Stored initial payment details in session ID: {}", session.getId());

            // Prepare redirect URL to frontend login page
            String redirectUrl = frontendUrl + "/login";
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment context created. Redirecting to login page.");
            response.put("redirectUrl", redirectUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[LoginController] Failed to initiate payment context: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to initiate payment context: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Handles redirect requests to the login page, typically after payment initiation.
     * Logs the payment ID if provided but relies primarily on session data.
     *
     * @param paymentId Optional payment identifier
     * @return ResponseEntity with redirect headers to the login page
     */
    @GetMapping("/api/payment/redirect")
    public ResponseEntity<?> redirectToLogin(@RequestParam(required = false) String paymentId) {
        if (paymentId != null) {
            log.warn("[LoginController] Received redirect request for paymentId {}, but session should handle context", paymentId);
        } else {
            log.info("[LoginController] Received redirect request without paymentId");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", frontendUrl + "/login");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Authenticates a client using email and password credentials.
     * On successful authentication, creates or updates the session with client information.
     * Checks for any pending payment initiation data in the session.
     *
     * @param loginRequest The login request containing email and password
     * @param request The HTTP request to access and modify the session
     * @return ResponseEntity with login response details including authentication status
     * @throws RuntimeException If an unexpected error occurs during authentication
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // Verify credentials
            boolean clientVerified = clientService.verifyCredentials(email, password);

            if (clientVerified) {
                Optional<Client> clientOpt = clientService.findByEmail(email);
                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();

                    // Create/update session
                    HttpSession session = request.getSession(true);
                    session.setAttribute(SESSION_CLIENT_ID_KEY, client.getId());
                    session.setAttribute(SESSION_CLIENT_EMAIL_KEY, client.getEmail());
                    log.info("[LoginController] Client ID {} and Email '{}' set in session {}",
                            client.getId(), client.getEmail(), session.getId());

                    // Check for initial payment data
                    Object initialAmount = session.getAttribute(SESSION_INIT_AMOUNT_KEY);
                    if (initialAmount != null) {
                        log.info("[LoginController] Found initial payment details in session for client {}", email);
                    }

                    // Create response
                    LoginResponse response = new LoginResponse(
                            session.getId(), // Use session ID as token
                            "success",
                            "Client authenticated successfully",
                            client.getId()
                    );
                    return ResponseEntity.ok().body(response);
                } else {
                    // Unlikely edge case
                    log.error("[LoginController] Client verified but could not be found for email: {}", email);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new LoginResponse(null, "error",
                                    "Internal server error retrieving client data", null));
                }
            }

            // Authentication failed
            log.warn("[LoginController] Invalid credentials for login attempt: {}", email);
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
                log.info("[LoginController] Invalidated existing session on login failure: {}", existingSession.getId());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, "error", "Invalid credentials", null));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}