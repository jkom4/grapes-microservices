// PaymentController.java
package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.TokenDTO;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.CardService;
import grapes.microservices.paymentbackend.services.PaymentService;
import grapes.microservices.paymentbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final CardService cardService;
    private final UserService userService;

    /**
     * Initiate 3D Secure verification for a payment
     * @param paymentRequest the payment request with card details
     * @param session the HTTP session
     * @return response with the verification status
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequestDTO paymentRequest, HttpSession session) {
        log.info("Initiating payment process");

        // Dans PaymentController.java, au début de initiatePayment

        log.info("Session ID in PaymentController: {}", session.getId()); // Log l'ID de session de la requête courante
        java.util.Collections.list(session.getAttributeNames()).forEach(name ->
                log.info("Session attribute - {}: {}", name, session.getAttribute(name)) // Log tous les attributs trouvés
        );

        // Puis votre logique existante
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            log.warn("User not authenticated (userId attribute is null in session)"); // Message plus précis
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "User session not found or expired. Please log in again."
                    ));
        }

        // Get user from session
        Optional<User> userOpt = userService.findByLogin((String) session.getAttribute("userLogin"));
        if (userOpt.isEmpty()) {
            log.warn("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "User not found"
                    ));
        }

        User user = userOpt.get();

        // Basic validation of payment request
        if (!paymentService.validateCardNumber(paymentRequest.getCardNumber())) {
            log.warn("Invalid card number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid card number"
                    ));
        }

        if (!paymentService.isAmountAcceptable(paymentRequest.getAmount())) {
            log.warn("Payment amount exceeds limit");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "Payment amount exceeds limit"
                    ));
        }

        // Initiate 3D Secure verification with ACS
        try {
            String otpCode = cardService.initiateCardVerification(paymentRequest, user);

            if (otpCode != null) {
                // Store payment request in session for later use
                session.setAttribute("pendingPaymentRequest", paymentRequest);

                // Generate a unique payment ID
                String paymentId = UUID.randomUUID().toString();
                session.setAttribute("paymentId", paymentId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Verification code sent to your phone");
                response.put("paymentId", paymentId);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "success", false,
                                "message", "Failed to initiate 3D Secure verification"
                        ));
            }
        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred: " + e.getMessage()
                    ));
        }
    }

    /**
     * Complete a payment after 3D Secure verification
     * @param tokenDTO the token DTO containing the verification code
     * @param session the HTTP session
     * @return response with the payment status
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody TokenDTO tokenDTO, HttpSession session) {
        log.info("Completing payment process with verification code");

        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            log.warn("User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "You must be logged in to complete a payment"
                    ));
        }

        // Get user from session
        Optional<User> userOpt = userService.findByLogin((String) session.getAttribute("userLogin"));
        if (userOpt.isEmpty()) {
            log.warn("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "User not found"
                    ));
        }

        User user = userOpt.get();

        // Get pending payment request from session
        PaymentRequestDTO paymentRequest = (PaymentRequestDTO) session.getAttribute("pendingPaymentRequest");
        if (paymentRequest == null) {
            log.warn("No pending payment request found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "No pending payment request found"
                    ));
        }

        // Process payment with verification code
        try {
            boolean paymentSuccess = paymentService.processPayment(tokenDTO.getPaymentToken(), paymentRequest, user);

            if (paymentSuccess) {
                // Clear pending payment request from session
                session.removeAttribute("pendingPaymentRequest");

                String paymentId = (String) session.getAttribute("paymentId");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                response.put("transactionId", paymentId != null ? paymentId : UUID.randomUUID().toString());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Payment verification failed"
                        ));
            }
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred: " + e.getMessage()
                    ));
        }
    }
}