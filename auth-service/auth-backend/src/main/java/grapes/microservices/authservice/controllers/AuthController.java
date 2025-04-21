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

@CrossOrigin
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // for rabbitMQ
        String userId = authService.getUserIdFromEmail(loginRequest.getEmail());
        String sourceIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String failureReason = null;

        try {
            String token = authService.getTokenFromChallenge(loginRequest);
            authService.sendAuthToQueue(userId, loginRequest.getAuthMethod(), sourceIp, userAgent,"Success", failureReason);
            // TODO : add refreshedToken
            return ResponseEntity.ok(new AuthResponse(token, ""));
        } catch (RuntimeException e) {
            failureReason = e.getMessage();
            authService.sendAuthToQueue(userId, loginRequest.getAuthMethod(), sourceIp, userAgent,"Failed", failureReason);
            return ResponseEntity.status(400).body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            failureReason = e.getMessage();
            authService.sendAuthToQueue(userId, loginRequest.getAuthMethod(), sourceIp, userAgent,"Failed", failureReason);
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
            return ResponseEntity.ok(new AuthResponse("", refreshedToken));
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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr(); // fallback
    }

}