package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.models.AuthMethod;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.SessionService;
import grapes.microservices.authservice.services.TokenService;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.services.auth.AbstractAuthProvider;
import grapes.microservices.authservice.services.auth.AuthMethodService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
     * Endpoint for user logout
     * Removes the session from the Redis database
     * @param token the JWT token
     * @return a response entity with the result of the logout attempt
     */
    /*@PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        try {
            token = token.substring(7); // Deletes
            String userId = tokenService.extractUserId(token);

            sessionService.deleteSession(userId);
            return ResponseEntity.ok("Logged out successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }*/


    /**
     * Endpoint to verify the challenge and get a JWT.
     * This method takes the challenge submitted by the user and generates a JWT.
     * @param email the email of the user
     * @param submittedChallenge the challenge submitted by the user
     * @return a response entity with the JWT if the challenge is correct
     */
    @PostMapping(value  = "/verify-challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyChallenge(@RequestParam String email, @RequestParam String submittedChallenge, @RequestParam AuthMethod authMethod, HttpServletResponse response) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(400).body("User not found.");
        }
        try {
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            String token = authProvider.processChallenge(user, submittedChallenge);
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            //cookie.setSecure(true); //when HTTPS is enabled
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);

            response.addCookie(cookie);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    /**
     * Endpoint to refresh the JWT token
     * This method takes the refresh token and generates a new JWT
     * @param refreshToken the refresh token
     * @return a response entity with the new JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        refreshToken = refreshToken.substring(7);
        String userId = tokenService.extractUserId(refreshToken);

        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return ResponseEntity.status(403).body("Invalid refresh token");
        }
        String email = userService.getUserById(userId).getEmail();
        String newAccessToken = tokenService.generateToken(email);
        return ResponseEntity.ok(newAccessToken);
    }
}
