// --- START OF payment-backend/src/main/java/grapes/microservices/paymentbackend/controllers/LoginController.java ---
package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest; // Gardé
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.UserService;
import jakarta.servlet.http.HttpServletRequest; // Import nécessaire
import jakarta.servlet.http.HttpSession; // Import nécessaire
import lombok.extern.slf4j.Slf4j; // Ajout de Slf4j pour les logs
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Import BigDecimal
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Important: allowCredentials
@Slf4j // Annotation pour le logger
public class LoginController {

    private final UserService userService;
    private static final String FRONTEND_URL = "http://localhost:3000";

    // Session attribute keys for initial payment data
    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";


    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint pour initier un paiement depuis un site externe (ou autre)
    @PostMapping("/api/login/payment-initiate")
    @ResponseBody
    // Ajout de HttpServletRequest pour accéder à la session
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateRequest paymentRequest, HttpServletRequest request) {
        try {
            BigDecimal amount = paymentRequest.getAmount();
            String clientId = paymentRequest.getClientId(); // Considéré comme l'ID/nom du marchand

            if (amount == null || clientId == null || clientId.isEmpty()) {
                log.warn("[LoginController] Missing amount or clientId in payment initiation request.");
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Amount and clientId (merchant identifier) are required"
                ));
            }

            // Générer un ID pour cette *tentative* d'initiation
            String initialPaymentId = UUID.randomUUID().toString();
            log.info("[LoginController] Payment initiated via external request - Initial ID: {}, Amount: {}, Merchant: {}",
                    initialPaymentId, amount, clientId);

            // --- STOCKAGE EN SESSION ---
            HttpSession session = request.getSession(true); // Obtient ou crée une session
            session.setAttribute(SESSION_INIT_AMOUNT_KEY, amount);
            session.setAttribute(SESSION_INIT_MERCHANT_KEY, clientId); // Stocke l'ID client comme nom de marchand
            session.setAttribute(SESSION_INIT_PAYMENT_ID_KEY, initialPaymentId);
            log.info("[LoginController] Stored initial payment details in session ID: {}", session.getId());
            // ---------------------------

            // Préparer la redirection vers la page de login du frontend
            String redirectUrl = FRONTEND_URL + "/login"; // Pas besoin de passer l'ID ici, il est en session
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

    // Endpoint de redirection (moins utile, mais gardé pour compatibilité potentielle)
    @GetMapping("/api/payment/redirect")
    public ResponseEntity<?> redirectToLogin(@RequestParam(required = false) String paymentId) { // Rend le param optionnel
        if (paymentId != null) {
            log.warn("[LoginController] Received redirect request for paymentId {}, but session should handle context. Redirecting to login.", paymentId);
        } else {
            log.info("[LoginController] Received redirect request without paymentId. Redirecting to login.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", FRONTEND_URL + "/login");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // Endpoint de connexion
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> connect(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            String login = loginRequest.getLogin();
            String password = loginRequest.getPassword();
            boolean userVerified = userService.verifyUser(login, password);

            if (userVerified) {
                Optional<User> userOpt = userService.findByLogin(login);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    HttpSession session = request.getSession(true); // Obtenir/Créer la session
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userLogin", user.getLogin());
                    log.info("[LoginController] User ID {} and Login '{}' set in session {}", user.getId(), user.getLogin(), session.getId());

                    Object initialAmount = session.getAttribute(SESSION_INIT_AMOUNT_KEY);
                    if (initialAmount != null) {
                        log.info("[LoginController] Found initial payment details in session for user {}.", login);
                    }

                    LoginResponse response = new LoginResponse(
                            session.getId(), // Utilise l'ID de session comme "token" - à adapter si JWT etc.
                            "success",
                            "User authenticated successfully",
                            user.getId()
                    );
                    return ResponseEntity.ok().body(response);
                } else {
                    // Cas très improbable si verifyUser réussit mais findByLogin échoue ensuite
                    log.error("[LoginController] User verified but could not be found for login: {}", login);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            new LoginResponse(null, "error", "Internal server error retrieving user data", null)
                    );
                }
            }

            // Échec authentification
            log.warn("[LoginController] Invalid credentials for login attempt: {}", login);
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
                log.info("[LoginController] Invalidated existing session on login failure: {}", existingSession.getId());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, "error", "Invalid credentials", null)
            );
        } catch (NoSuchAlgorithmException e) {
            log.error("[LoginController] Password hashing algorithm error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoginResponse(null, "error", "Internal server error during authentication", null)
            );
        }
    }
}
// --- END OF payment-backend/src/main/java/grapes/microservices/paymentbackend/controllers/LoginController.java ---