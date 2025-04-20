package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.CompletePaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.services.PaymentProcessingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller handling payment operations including initiation, verification, and completion.
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentProcessingService paymentProcessingService;

    /**
     * Initiates a payment process by validating client authentication, card details,
     * and creating a transaction record.
     *
     * @param paymentRequest The payment request containing card and amount details
     * @param session The HTTP session to retrieve client ID
     * @return ResponseEntity with success status, transactionId, or appropriate error message
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequestDTO paymentRequest, HttpSession session) {
        try {
            return paymentProcessingService.initiatePayment(paymentRequest, session);
        } catch (Exception e) {
            return paymentProcessingService.handleInitiationError(e);
        }
    }

    /**
     * Completes the payment process after OTP verification using transactionId.
     *
     * @param completeRequest DTO containing the payment verification token (OTP) and transactionId
     * @param session The HTTP session to retrieve client ID
     * @return ResponseEntity with payment result or appropriate error message
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@Valid @RequestBody CompletePaymentRequestDTO completeRequest, HttpSession session) {
        try {
            return paymentProcessingService.completePayment(completeRequest, session);
        } catch (Exception e) {
            return paymentProcessingService.handleCompletionError(e, completeRequest.getTransactionId());
        }
    }

    /**
     * Retrieves the details of a pending payment from the cache using transactionId.
     *
     * @param transactionId The ID of the transaction to retrieve details for.
     * @param session The HTTP session to retrieve client ID for authorization check.
     * @return ResponseEntity with payment details or appropriate error message.
     */
    @GetMapping("/pending-details")
    public ResponseEntity<?> getPendingPaymentDetails(@RequestParam @NotNull Long transactionId, HttpSession session) {
        try {
            return paymentProcessingService.getPendingPaymentDetails(transactionId, session);
        } catch (Exception e) {
            return paymentProcessingService.handlePendingDetailsError(e);
        }
    }
    /**
     * Retrieves payment details stored in the session.
     *
     * @param session The HTTP session containing payment details
     * @return ResponseEntity with payment details or error message
     */
    @GetMapping("/session-details")
    public ResponseEntity getSessionDetails(HttpSession session) {
        try {
            return paymentProcessingService.getSessionDetails(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving session details: " + e.getMessage()));
        }
    }

}