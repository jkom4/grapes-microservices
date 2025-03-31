package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.models.AuthMethod;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.services.auth.AbstractAuthProvider;
import grapes.microservices.authservice.services.auth.AuthMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthMethodService authMethodService;

    @Autowired
    private UserService userService;

    /**
     * Endpoint for user login
     * Generates and sends a challenge to the user's email if the credentials are correct
     * @param email the email of the user
     * @param password the password of the user
     * @return a response entity with the result of the login attempt
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, @RequestParam AuthMethod authMethod) throws IOException {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null || !user.verifyPassword(password)) {
                return ResponseEntity.status(400).body("Credentials are incorrect.");
            }
            if (!user.isEmailVerified()) {
                return ResponseEntity.status(400).body("The email is not verified.");
            }
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            authProvider.sendChallenge(user);
            return ResponseEntity.ok("Challenge sent to user by mail.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Endpoint to verify the challenge and get a JWT.
     * This method takes the challenge submitted by the user and generates a JWT.
     * @param email the email of the user
     * @param submittedChallenge the challenge submitted by the user
     * @return a response entity with the JWT if the challenge is correct
     */
    @PostMapping(value  = "/verify-challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyChallenge(@RequestParam String email, @RequestParam String submittedChallenge, @RequestParam AuthMethod authMethod) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(400).body("User not found.");
        }
        try {
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            String token = authProvider.processChallenge(user, submittedChallenge);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
