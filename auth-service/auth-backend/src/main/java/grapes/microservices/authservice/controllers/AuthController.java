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
    private TokenService tokenService;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, @RequestParam AuthMethod authMethod) throws IOException {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null || !user.verifyPassword(password)) {
                return ResponseEntity.status(401).body("Credentials are incorrect.");
            }
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            authProvider.sendChallenge(user);
            return ResponseEntity.ok("Challenge sent to user by : " + authMethod.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // TODO : implement CSRF protection and remove the comment then
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

    @PostMapping(value  = "/verify-challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyChallenge(@RequestParam String email, @RequestParam String submittedChallenge, @RequestParam AuthMethod authMethod, HttpServletResponse response) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(400).body("User not found.");
        }
        try {
            AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
            String token = authProvider.processChallenge(user, submittedChallenge);
            switch (authMethod) {
                case EMAIL:
                    if (!user.isEmailVerified()) {
                        user.setEmailVerified(true);
                    }
                    break;
            }
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            //cookie.setSecure(true); // TODO : remove comment when HTTPS is enabled
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);

            response.addCookie(cookie);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // TODO : implement CSRF protection
    /*@PostMapping("/refresh")
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
    }*/
}
