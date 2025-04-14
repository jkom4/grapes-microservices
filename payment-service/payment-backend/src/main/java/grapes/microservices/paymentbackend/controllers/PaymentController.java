package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.TokenDTO; // Gardé pour /complete
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest; // Gardé pour l'autre endpoint
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.CardService;
import grapes.microservices.paymentbackend.services.PaymentService;
import grapes.microservices.paymentbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired; // Assurez-vous que Autowired est importé si utilisé
import org.springframework.stereotype.Controller; // Assurez-vous que Controller est importé si utilisé

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collections; // Import Collections
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Locale; // Import Locale

@RestController // Utilise RestController si toutes les méthodes renvoient des ResponseBody
@RequestMapping("/api/payment") // Base path pour les endpoints de paiement
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final CardService cardService;
    private final UserService userService;

    // Clés de session (inchangées)
    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_PENDING_PAYMENT_KEY = "pendingPaymentRequest";
    private static final String SESSION_CURRENT_ATTEMPT_ID_KEY = "currentPaymentAttemptId";


    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequestDTO paymentRequestFromForm, HttpSession session) {
        log.info("[PaymentController] /initiate endpoint called.");
        log.debug("Session ID: {}", session.getId());

        // 1. Vérifier l'authentification utilisateur via la session
        Long userId = (Long) session.getAttribute("userId");
        String userLogin = (String) session.getAttribute("userLogin");
        if (userId == null || userLogin == null) {
            log.warn("User not authenticated (session missing userId/userLogin)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User session not found or expired. Please log in again."));
        }

        // 2. Récupérer l'objet User complet depuis la base de données
        Optional<User> userOpt = userService.findByLogin(userLogin);
        if (userOpt.isEmpty()) {
            log.error("User {} (ID {}) not found in DB despite being in session! Invalidating session.", userLogin, userId);
            session.invalidate();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User associated with session not found. Please log in again."));
        }
        User user = userOpt.get();
        log.info("User {} (ID {}) authenticated and retrieved.", user.getLogin(), user.getId());

        // 3. Fusionner les données initiales (session) et celles du formulaire
        PaymentRequestDTO finalPaymentRequest = paymentRequestFromForm;
        BigDecimal initialAmount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String initialMerchant = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);
        String initialPaymentId = (String) session.getAttribute(SESSION_INIT_PAYMENT_ID_KEY);

        if (initialAmount != null) {
            log.info("Using initial amount {} from session (overriding form amount if any: {})", initialAmount, paymentRequestFromForm.getAmount());
            finalPaymentRequest.setAmount(initialAmount);
        } else if (finalPaymentRequest.getAmount() == null) {
            log.error("Missing payment amount from both initial session and form for user {}", userLogin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Payment amount is missing"));
        }

        if (initialMerchant != null) {
            finalPaymentRequest.setMerchantName(initialMerchant);
        } else {
            finalPaymentRequest.setMerchantName("Grapes"); // Default
        }
        log.info("Payment details: Amount={}, Merchant={}", finalPaymentRequest.getAmount(), finalPaymentRequest.getMerchantName());

        // Génère ou récupère l'ID de tentative
        String paymentAttemptId = (initialPaymentId != null) ? initialPaymentId : UUID.randomUUID().toString();
        session.setAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY, paymentAttemptId);
        log.info("Using Payment Attempt ID: {}", paymentAttemptId);

        // Nettoyer les attributs initiaux de la session
        session.removeAttribute(SESSION_INIT_AMOUNT_KEY);
        session.removeAttribute(SESSION_INIT_MERCHANT_KEY);
        session.removeAttribute(SESSION_INIT_PAYMENT_ID_KEY);
        log.debug("Cleared initial payment attributes from session.");

        // --- DEBUT DES NOUVELLES VÉRIFICATIONS MÉTIER ---

        // 4. Vérification du Solde Utilisateur
        Double userBalance = user.getAccountBalance();
        // Vérification de nullité du solde et comparaison
        if (userBalance == null || userBalance < finalPaymentRequest.getAmount().doubleValue()) {
            log.warn("Insufficient balance for user {}. Balance: {}, Required: {}",
                    user.getLogin(), (userBalance == null ? "NULL" : userBalance), finalPaymentRequest.getAmount());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Insufficient account balance."));
        }
        log.info("Balance check passed for user {}.", user.getLogin());

        // 5. Vérification de la Correspondance du Numéro de Carte
        String storedCardNumber = user.getCardNumber();
        String enteredCardNumber = finalPaymentRequest.getCardNumber();
        // Vérification de nullité et comparaison
        if (storedCardNumber == null || !storedCardNumber.equals(enteredCardNumber)) {
            log.warn("Card number mismatch for user {}. Entered: ****{}, Stored: ****{}",
                    user.getLogin(),
                    // Ajout vérification nullité pour substring
                    enteredCardNumber != null ? enteredCardNumber.substring(Math.max(0, enteredCardNumber.length()-4)) : "null",
                    storedCardNumber != null ? storedCardNumber.substring(Math.max(0, storedCardNumber.length()-4)) : "null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "The card number entered does not match the registered card."));
        }
        log.info("Card number match check passed for user {}.", user.getLogin());

        // 6. Vérification de la Correspondance de la Date d'Expiration
        String storedExpiryDate = user.getCardExpiration();
        String enteredExpiryDate = finalPaymentRequest.getExpirationDate();
        // Vérification de nullité et comparaison
        if (storedExpiryDate == null || !storedExpiryDate.equals(enteredExpiryDate)) {
            log.warn("Expiration date mismatch for user {}. Entered: {}, Stored: {}",
                    user.getLogin(), enteredExpiryDate, storedExpiryDate);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "The expiration date entered does not match the registered card."));
        }
        log.info("Expiration date match check passed for user {}.", user.getLogin());

        // 7. Vérifications de Format (Existantes)
        if (!paymentService.validateCardNumber(enteredCardNumber)) {
            log.warn("Invalid card number format or Luhn check failed for user {}", user.getLogin());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid card number format."));
        }
        if (!paymentService.isExpirationDateValid(enteredExpiryDate)) {
            log.warn("Card expiration date is invalid or expired: {} for user {}", enteredExpiryDate, user.getLogin());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Card expiration date is invalid or expired."));
        }
        if (!paymentService.isAmountAcceptable(finalPaymentRequest.getAmount())) {
            log.warn("Payment amount exceeds limit or is invalid: {} for user {}", finalPaymentRequest.getAmount(), user.getLogin());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Payment amount is invalid or exceeds limit."));
        }
        log.info("Format checks passed for user {}.", user.getLogin());

        // --- FIN DES NOUVELLES VÉRIFICATIONS MÉTIER ---


        // 8. Si toutes les vérifications passent, stocker le DTO final en session et initier ACS
        try {
            session.setAttribute(SESSION_PENDING_PAYMENT_KEY, finalPaymentRequest);
            log.info("Stored final payment request details in session for user {}", userLogin);

            // *** CORRECTION DE L'APPEL ICI ***
            // Appel à ACS pour envoyer l'OTP etc., en passant les 3 arguments requis
            String otpCode = cardService.initiateCardVerification(
                    finalPaymentRequest, // 1. PaymentRequestDTO
                    user,                // 2. User
                    paymentAttemptId     // 3. String (ID de tentative/transaction)
            );
            // *******************************

            if (otpCode != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Verification required. Check your phone for the OTP code.");
                log.info("Verification initiated successfully for user {}, payment attempt ID: {}", user.getLogin(), paymentAttemptId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to initiate card verification with ACS for user {}", user.getLogin());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Failed to initiate 3D Secure verification with the bank. Please try again later."));
            }
        } catch (Exception e) {
            log.error("Error during payment initiation step for user {}: {}", user.getLogin(), e.getMessage(), e);
            session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
            session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An unexpected error occurred during payment initiation."));
        }
    }
    // --- Méthode /complete (inchangée par rapport à la version précédente qui gérait les retentatives) ---
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody TokenDTO tokenDTO, HttpSession session) {
        // ... (la logique de /complete reste celle qui gère les retentatives et appelle paymentService.processPayment) ...
        log.info("Completing payment process with verification code");

        Long userId = (Long) session.getAttribute("userId");
        String userLogin = (String) session.getAttribute("userLogin");
        if (userId == null || userLogin == null) {
            log.warn("User not authenticated for completing payment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User session not found or expired. Please log in again."));
        }

        Optional<User> userOpt = userService.findByLogin(userLogin);
        if (userOpt.isEmpty()) {
            log.warn("User {} (ID {}) not found in DB during completion", userLogin, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User associated with session not found."));
        }
        User user = userOpt.get();

        PaymentRequestDTO paymentRequest = (PaymentRequestDTO) session.getAttribute(SESSION_PENDING_PAYMENT_KEY);
        if (paymentRequest == null) {
            log.warn("No pending payment request found in session {} for user {}", session.getId(), user.getLogin());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "No pending payment request found or session expired. Please restart the payment."));
        }

        String paymentAttemptId = (String) session.getAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
        if (paymentAttemptId == null) {
            paymentAttemptId = "UNKNOWN-" + UUID.randomUUID().toString();
            log.warn("Payment Attempt ID not found in session {} for user {}. Using fallback: {}", session.getId(), user.getLogin(), paymentAttemptId);
        }

        try {
            // Appel à PaymentService qui contient maintenant la logique de vérification robuste ACK/NACK
            boolean paymentSuccess = paymentService.processPayment(tokenDTO.getPaymentToken(), paymentRequest, user);

            if (paymentSuccess) {
                log.info("Payment completed successfully for user {}, payment attempt ID: {}. Clearing session attributes.", user.getLogin(), paymentAttemptId);
                session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
                session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                return ResponseEntity.ok(response);

            } else {
                // L'OTP était incorrect (ou expiré/utilisé), mais le contexte de paiement existe toujours.
                log.warn("Payment verification failed (Invalid OTP) for user {}, payment attempt ID: {}", user.getLogin(), paymentAttemptId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST) // Utilise 400 Bad Request
                        .body(Map.of("success", false, "message", "Verification failed. Please check the code and try again."));
            }
        } catch (Exception e) {
            log.error("Error processing payment completion for user {}: {}", user.getLogin(), e.getMessage(), e);
            session.removeAttribute(SESSION_PENDING_PAYMENT_KEY);
            session.removeAttribute(SESSION_CURRENT_ATTEMPT_ID_KEY);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An internal error occurred during payment completion."));
        }
    }


    // --- Méthode /pending-details (inchangée) ---
    @GetMapping("/pending-details")
    public ResponseEntity<?> getPendingPaymentDetails(HttpSession session) {
        // ... (logique existante pour récupérer les détails depuis la session) ...
        log.info("Fetching pending payment details for session ID: {}", session.getId());

        Long userId = (Long) session.getAttribute("userId");
        String userLogin = (String) session.getAttribute("userLogin");
        if (userId == null || userLogin == null) {
            log.warn("Attempt to fetch pending details without active session.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User session not found or expired."));
        }

        PaymentRequestDTO pendingPayment = (PaymentRequestDTO) session.getAttribute(SESSION_PENDING_PAYMENT_KEY);

        if (pendingPayment == null) {
            log.warn("No pending payment found in session {} for user {}", session.getId(), userLogin);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No pending payment found."));
        }

        try {
            String maskedCardNumber = paymentService.maskCardNumber(pendingPayment.getCardNumber());
            String formattedAmount = String.format(Locale.FRANCE, "EURO %.2f", pendingPayment.getAmount()); // Formatage FR
            String merchantName = pendingPayment.getMerchantName() != null ? pendingPayment.getMerchantName() : "Grapes";

            Map<String, Object> details = new HashMap<>();
            details.put("success", true);
            details.put("merchantName", merchantName);
            details.put("amount", formattedAmount);
            details.put("maskedCardNumber", maskedCardNumber);

            log.info("Returning pending payment details for user {}: Merchant={}, Amount={}, Card={}",
                    userLogin, merchantName, formattedAmount, maskedCardNumber);

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            log.error("Error retrieving pending payment details for user {}: {}", userLogin, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving payment details."));
        }
    }

} // Fin de la classe PaymentController