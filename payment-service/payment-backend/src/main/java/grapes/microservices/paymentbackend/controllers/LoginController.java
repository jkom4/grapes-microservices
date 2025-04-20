package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.services.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller handling user authentication operations.
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * Initiates a payment process by capturing payment details and storing them in the session.
     *
     * @param paymentRequest The payment request containing amount and merchant identifier
     * @param request The HTTP request to access and modify the session
     * @return ResponseEntity containing success/error status and redirect URL
     */
    @PostMapping("/payment-initiate")
    @ResponseBody
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateRequest paymentRequest, HttpServletRequest request) {
        try {
            Map<String, Object> response = loginService.initiatePayment(paymentRequest, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to initiate payment context: " + e.getMessage()
            ));
        }
    }

    /**
     * Authenticates a client using email and password credentials.
     *
     * @param loginRequest The login request containing email and password
     * @param request The HTTP request to access and modify the session
     * @return ResponseEntity with login response details including authentication status
     */
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            LoginResponse response = loginService.login(loginRequest, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(
                    new LoginResponse(null, "error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new LoginResponse(null, "error", "Internal server error: " + e.getMessage(), null)
            );
        }
    }
}