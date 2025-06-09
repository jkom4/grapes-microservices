package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.models.Client;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final ClientService clientService;
    @Value("${app.grapes.front.url}")
    private String defaultRedirectUrl;

    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_ORDER_KEY = "initialOrderID";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_INIT_REDIRECT_URL_KEY = "initialRedirectUrl";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";
    private static final String SESSION_CLIENT_EMAIL_KEY = "clientEmail";

    public Map<String, Object> initiatePayment(PaymentInitiateRequest paymentRequest, HttpServletRequest request) {
        BigDecimal amount = paymentRequest.getAmount();
        String merchantId = paymentRequest.getMerchantId();
        Long orderId = paymentRequest.getOrderId();
        String redirectUrl = paymentRequest.getRedirectUrl();

        if (amount == null || merchantId == null || merchantId.isEmpty()) {
            log.warn("[LoginService] Missing data in initiation request");
            throw new IllegalArgumentException("Amount and merchantId (merchant identifier) are required");
        }

        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = defaultRedirectUrl;
            log.info("[LoginService] Redirect URL not provided, using default value");
        }

        String initialPaymentId = UUID.randomUUID().toString();
        log.info("[LoginService] Payment initiated - ID: {}, Amount: {}, Merchant: {}, URL: {}",
                initialPaymentId, amount, merchantId, redirectUrl);

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_INIT_AMOUNT_KEY, amount);
        session.setAttribute(SESSION_INIT_MERCHANT_KEY, merchantId);
        session.setAttribute(SESSION_INIT_ORDER_KEY, orderId);
        session.setAttribute(SESSION_INIT_PAYMENT_ID_KEY, initialPaymentId);
        session.setAttribute(SESSION_INIT_REDIRECT_URL_KEY, redirectUrl);
        log.info("[LoginService] Payment details stored in session ID: {}", session.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment context created. Redirecting to login page.");
        response.put("redirectUrl", defaultRedirectUrl);

        return response;
    }

    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        boolean clientVerified = clientService.verifyCredentials(email, password);

        if (!clientVerified) {
            log.warn("[LoginService] Login attempt with invalid credentials: {}", email);

            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
                log.info("[LoginService] Session invalidated after login failure: {}", existingSession.getId());
            }

            throw new IllegalArgumentException("Invalid credentials");
        }

        Optional<Client> clientOpt = clientService.findByEmail(email);
        if (clientOpt.isEmpty()) {
            log.error("[LoginService] Client not found for email: {}", email);
            throw new IllegalStateException("Internal server error retrieving client data");
        }

        Client client = clientOpt.get();

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_CLIENT_ID_KEY, client.getId());
        session.setAttribute(SESSION_CLIENT_EMAIL_KEY, client.getEmail());
        log.info("[LoginService] Client ID {} and Email '{}' stored in session {}",
                client.getId(), client.getEmail(), session.getId());

        Object initialAmount = session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        if (initialAmount != null) {
            log.info("[LoginService] Payment details found in session for client {}", email);
        }

        return new LoginResponse(
                session.getId(),
                "success",
                "Client authenticated successfully",
                client.getId()
        );
    }
}