package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.PaymentRequest;
import grapes.microservices.paymentbackend.dto.PaymentVerificationRequest;
import grapes.microservices.paymentbackend.models.Transaction;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.TransactionService;
import grapes.microservices.paymentbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PaymentController {

    private final UserService userService;
    private final TransactionService transactionService;

    @Autowired
    public PaymentController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GetMapping("/payment")
    public String paymentPage(RedirectAttributes redirectAttributes) {
        if (!LoginController.isConnected()) {
            redirectAttributes.addFlashAttribute("error", "You need to be logged in to access the payment page.");
            return "redirect:/login";
        }
        return "paymentotp";
    }

    @PostMapping("/api/payment/initiate")
    public ResponseEntity<Object> initiatePayment(@RequestBody PaymentRequest paymentRequest) {
        if (!LoginController.isConnected()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("error", "User not authenticated"));
        }

        Long userId = LoginController.getConnectedUserId();
        Optional<User> userOpt = userService.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse("error", "User not found"));
        }

        User user = userOpt.get();

        // Initiate the transaction
        Transaction transaction = transactionService.initiateTransaction(
                user,
                paymentRequest.getAmount(),
                paymentRequest.getMerchant()
        );

        if (transaction.getStatus() == Transaction.TransactionStatus.FAILED) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse("error", "Insufficient funds"));
        }

        if (transaction.getStatus() == Transaction.TransactionStatus.PENDING) {
            return ResponseEntity.ok()
                    .body(new PaymentInitiationResponse(
                            transaction.getId(),
                            transaction.getStatus().toString(),
                            "Verification code sent to your phone"
                    ));
        }

        return ResponseEntity.status(500)
                .body(new ApiResponse("error", "Failed to initiate payment"));
    }

    @PostMapping("/payment")
    public ResponseEntity<Object> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        if (!LoginController.isConnected()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("error", "User not authenticated"));
        }

        String paymentToken = verificationRequest.getPaymentToken();

        System.out.println("[INFO] Token: " + paymentToken);

        boolean tokenIsValid = transactionService.completeTransaction(paymentToken);

        if (tokenIsValid) {
            return ResponseEntity.ok()
                    .body(new ApiResponse("success", "Payment successful"));
        }

        return ResponseEntity.status(401)
                .body(new ApiResponse("error", "Token is invalid, payment failed"));
    }

    // Inner classes for response structures
    private static class ApiResponse {
        private String status;
        private String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class PaymentInitiationResponse extends ApiResponse {
        private Long transactionId;

        public PaymentInitiationResponse(Long transactionId, String status, String message) {
            super(status, message);
            this.transactionId = transactionId;
        }

        public Long getTransactionId() {
            return transactionId;
        }
    }
}