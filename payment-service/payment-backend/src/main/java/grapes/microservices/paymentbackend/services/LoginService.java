package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.models.Client;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service handling login related operations and payment initiations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final ClientService clientService;

    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";
    private static final String SESSION_CLIENT_EMAIL_KEY = "clientEmail";


    /**
     * Initiates a payment by storing payment details in the session.
     *
     * @param paymentRequest The payment initiation request
     * @param request The HTTP request
     * @return Map containing status, message and redirect URL
     */
    public Map<String, Object> initiatePayment(PaymentInitiateRequest paymentRequest, HttpServletRequest request) {
        BigDecimal amount = paymentRequest.getAmount();
        String merchantId = paymentRequest.getMerchantId();

        if (amount == null || merchantId == null || merchantId.isEmpty()) {
            log.warn("[LoginService] Missing amount or merchantId in payment initiation request");
            throw new IllegalArgumentException("Amount and merchantId (merchant identifier) are required");
        }

        // Generate ID for this initiation attempt
        String initialPaymentId = UUID.randomUUID().toString();
        log.info("[LoginService] Payment initiated via external request - Initial ID: {}, Amount: {}, Merchant: {}",
                initialPaymentId, amount, merchantId);

        // Store in session
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_INIT_AMOUNT_KEY, amount);
        session.setAttribute(SESSION_INIT_MERCHANT_KEY, merchantId);
        session.setAttribute(SESSION_INIT_PAYMENT_ID_KEY, initialPaymentId);
        log.info("[LoginService] Stored initial payment details in session ID: {}", session.getId());


        String redirectUrl = "http://localhost:3000/login";
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment context created. Redirecting to login page.");
        response.put("redirectUrl", redirectUrl);

        return response;
    }

    /**
     * Authenticates a client using email and password credentials.
     *
     * @param loginRequest The login request details
     * @param request The HTTP request
     * @return LoginResponse containing authentication result
     */
    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Verify credentials
        boolean clientVerified = clientService.verifyCredentials(email, password);

        if (!clientVerified) {
            log.warn("[LoginService] Invalid credentials for login attempt: {}", email);

            // Invalidate existing session if present
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
                log.info("[LoginService] Invalidated existing session on login failure: {}", existingSession.getId());
            }

            throw new IllegalArgumentException("Invalid credentials");
        }

        // Get client details
        Optional<Client> clientOpt = clientService.findByEmail(email);
        if (clientOpt.isEmpty()) {
            log.error("[LoginService] Client verified but could not be found for email: {}", email);
            throw new IllegalStateException("Internal server error retrieving client data");
        }

        Client client = clientOpt.get();

        // Create/update session
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_CLIENT_ID_KEY, client.getId());
        session.setAttribute(SESSION_CLIENT_EMAIL_KEY, client.getEmail());
        log.info("[LoginService] Client ID {} and Email '{}' set in session {}",
                client.getId(), client.getEmail(), session.getId());

        // Check for initial payment data
        Object initialAmount = session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        if (initialAmount != null) {
            log.info("[LoginService] Found initial payment details in session for client {}", email);
        }

        // Create response
        return new LoginResponse(
                session.getId(), // Use session ID as token
                "success",
                "Client authenticated successfully",
                client.getId()
        );
    }
}