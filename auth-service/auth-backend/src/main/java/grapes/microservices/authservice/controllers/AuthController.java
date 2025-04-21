package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.dto.EIDRegisterDTO;
import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.*;
import grapes.microservices.authservice.services.*;
import grapes.microservices.authservice.services.auth.AbstractAuthProvider;
import grapes.microservices.authservice.services.auth.AuthMethodService;
import grapes.microservices.authservice.services.eid.EIDCardService;
import grapes.microservices.authservice.services.eid.EIDVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private EIDCardService eidCardService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EIDVerificationService eidVerificationService;

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

    @PostMapping(value = "/verify-challenge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyChallenge(@RequestParam String email, @RequestParam String submittedChallenge, @RequestParam AuthMethod authMethod, @RequestParam(required = false) String pin, HttpServletResponse response) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(400).body("User not found.");
        }
        try {
            String token;
            if (authMethod == AuthMethod.EID) {
                if (pin == null) {
                    return ResponseEntity.badRequest().body("PIN required for eID verification");
                }
                boolean valid = eidVerificationService.verifyEIDChallenge(email, submittedChallenge, pin);
                if (!valid) {
                    return ResponseEntity.status(401).body(" invalid eID Signature ");
                }
                token = tokenService.generateToken(user.getEmail());
            } else {
                AbstractAuthProvider authProvider = authMethodService.getAuthProvider(authMethod);
                token = authProvider.processChallenge(user, submittedChallenge);
                if (authMethod == AuthMethod.EMAIL && !user.isEmailVerified()) {
                    user.setEmailVerified(true);
                }
            }
            sessionService.deleteSession(user.getId().toHexString());
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Runtime error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/eid/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginWithEID() {
        try {
            EIDCardInfo cardInfo = eidCardService.readCard();
            UserDTO userDTO = userMapper.toDTOFromEID(cardInfo);

            Optional<User> existingUser = userService.getUserByNationalId(userDTO.getNationalId());

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getEmail() != null && user.getPhoneNumber() != null && user.getPassword() != null) {
                    return ResponseEntity.ok(userMapper.toDTO(user));
                } else {
                    return ResponseEntity.status(307)
                            .header("Location", "/auth/eid/register")
                            .body("Incomplete information. Redirecting to /auth/eid/register");
                }
            } else {
                return ResponseEntity.status(307)
                        .header("Location", "/auth/eid/register")
                        .body("User not found. Redirecting to /auth/eid/register");
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("eID middleware not found")) {
                return ResponseEntity.status(500).body("eID error: Missing middleware. Please install from https://eid.belgium.be/en/download/15/license");
            } else if (message.contains("No card detected")) {
                return ResponseEntity.status(400).body("eID error: No card detected in reader");
            }
            return ResponseEntity.status(500).body("eID login error: " + e.getMessage());
        }
    }

    @PostMapping("/eid/register")
    @Operation(summary = "Register with eID", description = "Register a user using data from the Belgian eID card and additional fields")
    public ResponseEntity<?> registerWithEID(@Valid @RequestBody EIDRegisterDTO additionalInfo) {
        try {
            EIDCardInfo cardInfo = eidCardService.readCard();
            UserDTO userDTO = userMapper.toDTOFromEID(cardInfo);

            userDTO.setEmail(additionalInfo.getEmail());
            userDTO.setPhoneNumber(additionalInfo.getPhoneNumber());
            userDTO.setPassword(additionalInfo.getPassword());
            userDTO.setGender(cardInfo.getGender());

            AuthMean emailAuth = new AuthMean();
            emailAuth.setName(AuthMethod.EMAIL);
            emailAuth.setEnabled(true);
            emailAuth.setLastLogin(new Date());

            Map<String, AuthMean> authMethods = new HashMap<>();
            authMethods.put("EMAIL", emailAuth);
            userDTO.setAuthMethods(authMethods);

            Optional<User> existing = userService.getUserByNationalId(userDTO.getNationalId());
            if (existing.isPresent()) {
                return ResponseEntity.status(409).body("A user with this national ID already exists.");
            }

            User newUser = userMapper.toEntity(userDTO);
            User saved = userService.registerUser(newUser);

            return ResponseEntity.ok(userMapper.toDTO(saved));
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("eID middleware not found")) {
                return ResponseEntity.status(500).body("eID error: Missing middleware. Please install from https://eid.belgium.be/en/download/15/license");
            } else if (message.contains("No card detected")) {
                return ResponseEntity.status(400).body("eID error: No card detected in reader");
            }
            return ResponseEntity.status(500).body(" eID (register) Error : " + e.getMessage());
        }
    }
}
