package grapes.microservices.paymentbackend.controllers;

import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.services.UserService;
import grapes.microservices.paymentbackend.utils.AesCryptoUtils;
import grapes.microservices.paymentbackend.utils.ChallengeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "*")
public class ChallengeAuthController {

    private final UserService userService;

    // Stockage des challenges actifs
    private static final ConcurrentHashMap<String, ChallengeSession> activeChallenges = new ConcurrentHashMap<>();

    @Autowired
    public ChallengeAuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 1. Requête initiale (Client → Banque)
     * Nom: Demande d'authentification/challenge
     * Méthode: POST /api/auth/otp/initiate
     */
    @PostMapping("/api/auth/otp/initiate")
    public ResponseEntity<Map<String, Object>> initiateChallenge(@RequestBody Map<String, String> request) {
        // Extraire les données de la requête
        String clientId = request.get("clientId");
        String passNumber = request.get("passNumber");
        String nationalId = request.get("nationalId");

        Map<String, Object> response = new HashMap<>();

        // Validation des paramètres
        if (clientId == null || passNumber == null || nationalId == null) {
            response.put("status", "error");
            response.put("message", "Missing required parameters");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérifier l'utilisateur dans la base de données
        Optional<User> userOpt = userService.findByLogin(clientId);

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return ResponseEntity.status(401).body(response);
        }

        // Générer un challenge unique
        String sessionId = UUID.randomUUID().toString();
        String challenge = ChallengeGenerator.generateChallenge();

        // Stocker la session avec le challenge
        ChallengeSession session = new ChallengeSession(clientId, challenge, System.currentTimeMillis());
        activeChallenges.put(sessionId, session);

        // Log pour le débogage (à supprimer en production)
        System.out.println("[DEBUG] New challenge created: " + challenge + " for session: " + sessionId);

        // 2. Réponse (Banque → Client)
        // Nom: Envoi du challenge
        response.put("status", "success");
        response.put("sessionId", sessionId);
        response.put("challenge", challenge);

        return ResponseEntity.ok(response);
    }

    /**
     * 3. Requête finale (Client → Banque)
     * Nom: Soumission du digest/réponse
     * Méthode: POST /api/auth/validate
     */
    @PostMapping("/api/auth/validate")
    public ResponseEntity<Map<String, Object>> validateChallenge(@RequestBody Map<String, String> request) {
        // Extraire les données de la requête
        String digest = request.get("digest");
        String sessionId = request.get("sessionId");

        Map<String, Object> response = new HashMap<>();

        // Validation des paramètres
        if (digest == null || sessionId == null) {
            response.put("status", "error");
            response.put("message", "Missing required parameters");
            return ResponseEntity.badRequest().body(response);
        }

        // Récupérer la session de challenge
        ChallengeSession session = activeChallenges.get(sessionId);

        if (session == null) {
            response.put("status", "error");
            response.put("message", "Invalid or expired session");
            return ResponseEntity.status(401).body(response);
        }

        // Vérifier l'expiration (5 minutes)
        long currentTime = System.currentTimeMillis();
        if (currentTime - session.getCreatedAt() > 5 * 60 * 1000) {
            activeChallenges.remove(sessionId);
            response.put("status", "error");
            response.put("message", "Challenge expired");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Récupérer l'utilisateur
            Optional<User> userOpt = userService.findByLogin(session.getClientId());

            if (userOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();

            // Vérifier le digest avec AES-256
            boolean isValid = AesCryptoUtils.validateChallengeResponse(digest, session.getChallenge(), user.getPassword());

            // 4. Réponse finale (Banque → Client)
            // Nom: Confirmation d'authentification
            if (isValid) {
                // Authentification réussie
                String authToken = UUID.randomUUID().toString();

                // Supprimer le challenge utilisé
                activeChallenges.remove(sessionId);

                response.put("status", "success");
                response.put("message", "Authentication successful");
                response.put("authToken", authToken);
                response.put("userId", user.getId());

                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid authentication response");
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Classe interne pour stocker les informations de session de challenge
     */
    private static class ChallengeSession {
        private final String clientId;
        private final String challenge;
        private final long createdAt;

        public ChallengeSession(String clientId, String challenge, long createdAt) {
            this.clientId = clientId;
            this.challenge = challenge;
            this.createdAt = createdAt;
        }

        public String getClientId() {
            return clientId;
        }

        public String getChallenge() {
            return challenge;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}