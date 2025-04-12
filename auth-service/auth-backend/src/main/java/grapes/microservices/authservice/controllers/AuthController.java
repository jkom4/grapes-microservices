package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.AuthResponse;
import grapes.microservices.authservice.dto.JsonMessage;
import grapes.microservices.authservice.dto.ChallengeRequest;
import grapes.microservices.authservice.dto.LoginRequest;
import grapes.microservices.authservice.services.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendChallenge(@RequestBody ChallengeRequest challengeRequest) {
        try {
            authService.sendChallenge(challengeRequest);
            return ResponseEntity.ok(new JsonMessage("Challenge sent by : " + challengeRequest.getAuthMethod().getName()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new JsonMessage(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new JsonMessage(e.getMessage()));
        }
    }

    @PostMapping(value  = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.getTokenFromChallenge(loginRequest);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new JsonMessage(e.getMessage()));
        }
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonMessage> logout(HttpServletRequest request) {
        try {
            String token = authService.checkUserIsAuthenticated(request);
            authService.logout(token);
            return ResponseEntity.ok(new JsonMessage("Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new JsonMessage(e.getMessage()));
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            String refreshedToken = authService.getRefreshToken(token);
            return ResponseEntity.ok(new AuthResponse(refreshedToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new JsonMessage(e.getMessage()));
        }
    }

    @GetMapping(value = "/session", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            if (authService.checkSession(token)) {
                return ResponseEntity.ok(new JsonMessage("Session is valid"));
            } else {
                return ResponseEntity.status(401).body(new JsonMessage("Session is invalid"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}