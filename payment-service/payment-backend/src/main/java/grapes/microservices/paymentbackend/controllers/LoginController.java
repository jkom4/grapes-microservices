package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest; // Gardé au cas où vous l'utiliseriez ailleurs
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.UserService;
import jakarta.servlet.http.HttpServletRequest; // Import nécessaire
import jakarta.servlet.http.HttpSession; // Import nécessaire
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

    // !! RETIREZ CES VARIABLES STATIQUES !! Elles sont remplacées par la gestion de session HTTP
    // private static boolean IS_CONNECTED = false;
    // private static Long CONNECTED_USER_ID = null;

    // Endpoint pour initier un paiement avec un format simplifié (inchangé a priori)
    @PostMapping("/api/login/payment-initiate")
    @ResponseBody
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateRequest paymentRequest) {
        try {
            if (paymentRequest.getAmount() == null || paymentRequest.getClientId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Amount and clientId are required"
                ));
            }
            String paymentId = UUID.randomUUID().toString();
            System.out.println("[INFO] Payment initiated - ID: " + paymentId
                    + ", Amount: " + paymentRequest.getAmount()
                    + ", Client: " + paymentRequest.getClientId());

            String redirectUrl = FRONTEND_URL + "/login";
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

    // Endpoint de redirection (inchangé a priori)
    @GetMapping("/api/payment/redirect")
    public ResponseEntity<?> redirectToLogin(@RequestParam String paymentId) {
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

    // Endpoint de connexion (corrigé)
    @PostMapping("/api/login")
    @ResponseBody
    // Ajoutez HttpServletRequest comme paramètre pour accéder à la session
    public ResponseEntity<LoginResponse> connect(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            String login = loginRequest.getLogin();
            String password = loginRequest.getPassword();

            boolean userVerified = userService.verifyUser(login, password);

            if (userVerified) {
                Optional<User> userOpt = userService.findByLogin(login);
                System.out.println("[INFO] User found: " + userOpt.isPresent());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // ** GESTION DE LA SESSION HTTP **
                    HttpSession session = request.getSession(true); // Obtenir/Créer la session
                    session.setAttribute("userId", user.getId());     // Stocker l'ID utilisateur
                    session.setAttribute("userLogin", user.getLogin()); // Stocker le login utilisateur

                    System.out.println("[INFO] User ID " + user.getId() + " and Login '" + user.getLogin() + "' set in session " + session.getId());

                    String sessionToken = UUID.randomUUID().toString(); // Peut être utilisé pour autre chose si besoin

                    LoginResponse response = new LoginResponse(
                            sessionToken,
                            "success",
                            "User authenticated successfully",
                            user.getId()
                    );

                    return ResponseEntity.ok().body(response);
                }
            }

            // Échec de l'authentification ou utilisateur non trouvé
            // Optionnel: invalider toute session existante associée à cette requête
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
                System.out.println("[INFO] Invalidated existing session on login failure: " + existingSession.getId());
            }

            return ResponseEntity.status(401).body(
                    new LoginResponse(
                            null,
                            "error",
                            "Invalid credentials",
                            null
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            // Loggez l'erreur serveur
            System.err.println("[ERROR] Password hashing algorithm not found: " + e.getMessage());
            return ResponseEntity.status(500).body(
                    new LoginResponse(
                            null,
                            "error",
                            "Internal server error during authentication",
                            null
                    )
            );
        }
    }

    // !! RETIREZ CES MÉTHODES STATIQUES !! Elles ne sont plus pertinentes
    // public static boolean isConnected() {
    //     return IS_CONNECTED;
    // }
    // public static Long getConnectedUserId() {
    //     return CONNECTED_USER_ID;
    // }
}