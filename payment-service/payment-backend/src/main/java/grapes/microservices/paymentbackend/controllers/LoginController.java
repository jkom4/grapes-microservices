package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@CrossOrigin(origins = "http://localhost:3000") // Autorise les requêtes du frontend React
public class LoginController {

    private final UserService userService;

    // URL du frontend React
    private static final String FRONTEND_URL = "http://localhost:3000";

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    private static boolean IS_CONNECTED = false;
    private static Long CONNECTED_USER_ID = null;

    // Endpoint pour initier un paiement avec un format simplifié
    @PostMapping("/api/login/payment-initiate")
    @ResponseBody
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateRequest paymentRequest) {
        try {
            // Vérification des données minimales requises
            if (paymentRequest.getAmount() == null || paymentRequest.getClientId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Amount and clientId are required"
                ));
            }

            // Générer un ID de paiement unique
            String paymentId = UUID.randomUUID().toString();

            // En production, vous stockeriez ces informations dans une base de données
            System.out.println("[INFO] Payment initiated - ID: " + paymentId
                    + ", Amount: " + paymentRequest.getAmount()
                    + ", Client: " + paymentRequest.getClientId());

            // Créer l'URL de redirection vers le frontend
            String redirectUrl = FRONTEND_URL + "/login";

            // Créer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment initiated. Redirecting to login page.");
            response.put("redirectUrl", redirectUrl);
            response.put("paymentId", paymentId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to initiate payment: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/api/payment/redirect")
    public ResponseEntity<?> redirectToLogin(@RequestParam String paymentId) {
        // Vérifier si le paymentId est valide
        if (paymentId != null && !paymentId.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", FRONTEND_URL + "/login?paymentId=" + paymentId);

            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid payment ID"
            ));
        }
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> connect(@RequestBody LoginRequest loginRequest) {
        try {
            String login = loginRequest.getLogin();
            String password = loginRequest.getPassword();

            boolean userVerified = userService.verifyUser(login, password);

            if (userVerified) {
                Optional<User> userOpt = userService.findByLogin(login);
                System.out.println("[INFO] User found: " + userOpt.isPresent());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    IS_CONNECTED = true;
                    CONNECTED_USER_ID = user.getId();

                    String sessionToken = UUID.randomUUID().toString();

                    LoginResponse response = new LoginResponse(
                            sessionToken,
                            "success",
                            "User authenticated successfully",
                            user.getId()
                    );

                    return ResponseEntity.ok().body(response);
                }
            }

            IS_CONNECTED = false;
            CONNECTED_USER_ID = null;

            return ResponseEntity.status(401).body(
                    new LoginResponse(
                            null,
                            "error",
                            "Invalid credentials",
                            null
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            return ResponseEntity.status(500).body(
                    new LoginResponse(
                            null,
                            "error",
                            "Internal server error",
                            null
                    )
            );
        }
    }

    public static boolean isConnected() {
        return IS_CONNECTED;
    }

    public static Long getConnectedUserId() {
        return CONNECTED_USER_ID;
    }
}