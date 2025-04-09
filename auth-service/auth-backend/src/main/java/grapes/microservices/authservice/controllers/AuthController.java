package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.models.AuthMethod;
import grapes.microservices.authservice.models.AuthResponse;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.SessionService;
import grapes.microservices.authservice.services.TokenService;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.services.auth.AbstractAuthProvider;
import grapes.microservices.authservice.services.auth.AuthMethodService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthMethodService authMethodService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TokenService tokenService;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, @RequestParam AuthMethod authMethod) throws IOException {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null || !user.verifyPassword(password)) {
                return ResponseEntity.status(401).body("Credentials are incorrect.");
            }
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            if (authProvider.sendChallenge(user)) {
                return ResponseEntity.ok("Challenge sent to user by : " + authMethod.getName());
            } else {
                return ResponseEntity.status(400).body("Failed to send challenge.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        try {
            token = token.substring(7); // Deletes
            String userId = tokenService.extractUserId(token);

            sessionService.deleteSession(userId);
            return ResponseEntity.ok("Logged out successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping(value  = "/verify-challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyChallenge(@RequestParam String email, @RequestParam String submittedChallenge, @RequestParam AuthMethod authMethod) {
        try {
            User user = userService.getUserByEmail(email);
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            //reset session
            sessionService.resetSession(user.getId().toHexString());
            //new session
            String token = authProvider.processChallenge(user, submittedChallenge);
            switch (authMethod) {
                case EMAIL:
                    if (!user.isEmailVerified()) {
                        user.setEmailVerified(true);
                    }
                case SMS:
                    if (!user.isPhoneVerified()) {
                        user.setPhoneVerified(true);
                    }
                    break;
            }
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // TODO : finish refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Authorization");
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        refreshToken = refreshToken.substring(7);
        String userId = tokenService.extractUserId(refreshToken);

        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return ResponseEntity.status(403).body("Invalid refresh token");
        }
        String newAccessToken = tokenService.generateToken(userId);
        return ResponseEntity.ok(newAccessToken);
    }

    @GetMapping(value = "/session", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        try {
            token = token.substring(7); // Deletes "Bearer "
            String userId = tokenService.extractUserId(token);
            String session = sessionService.getSession(userId);
            if (session == null) {
                return ResponseEntity.status(404).body("Session not found");
            }
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
