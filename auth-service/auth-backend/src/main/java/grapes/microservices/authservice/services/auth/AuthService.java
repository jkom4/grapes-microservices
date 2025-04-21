package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.dto.ChallengeRequest;
import grapes.microservices.authservice.dto.LoginRequest;
import grapes.microservices.authservice.models.AuthMean;
import grapes.microservices.authservice.models.AuthMethod;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.SessionService;
import grapes.microservices.authservice.services.TokenService;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.utils.exceptions.ChallengeSendFailedException;
import grapes.microservices.authservice.utils.exceptions.InvalidCredentialsException;
import grapes.microservices.authservice.utils.exceptions.UnauthorizedException;
import grapes.microservices.authservice.utils.exceptions.UserNotActiveException;
import grapes.microservices.authservice.dto.AuthEventPayload;
import grapes.microservices.authservice.services.AuthEventProducer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static grapes.microservices.authservice.models.User.isPasswordFormatValid;
import static org.mariadb.jdbc.plugin.authentication.standard.ed25519.Utils.bytesToHex;

/**
 * AuthService is responsible for handling authentication-related operations.
 *
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthMethodService authMethodService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private final AuthEventProducer producer;


    /**
     * Sends a challenge to the user for authentication.
     *
     * @param challengeRequest the login request containing user credentials
     */
    public void sendChallenge(ChallengeRequest challengeRequest) throws IOException {
        User user = userService.getUserByEmail(challengeRequest.getEmail());
        if (!user.isActive()) {
            throw new UserNotActiveException("User is not active. Please contact support.");
        }
        if (!user.verifyUserPassword(challengeRequest.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!isPasswordFormatValid(challengeRequest.getPassword())) {
            user.setPasswordValid(false);
        }

        // set raw password to user
        user.setPassword(challengeRequest.getPassword());
        AbstractAuthProvider authProvider = authMethodService.getAuthProvider(challengeRequest.getAuthMethod());
        boolean challengeSent = authProvider.sendChallenge(user);

        if (!challengeSent) {
            throw new ChallengeSendFailedException();
        }
    }

    /**
     * Processes the challenge submitted by the user and generates a token if successful.
     *
     * @param loginRequest the request containing the email, submitted challenge, and auth method
     */
    public String getTokenFromChallenge(LoginRequest loginRequest) throws Exception {
        User user = userService.getUserByEmail(loginRequest.getEmail());
        AbstractAuthProvider authProvider = authMethodService.getAuthProvider(loginRequest.getAuthMethod());

        boolean isValid = verifyDigest(user, loginRequest, authProvider);
        if (!isValid) {
            throw new IllegalArgumentException("Invalid challenge or PIN code");
        }
        // delete the challenge
        authProvider.deleteChallenge(user);
        // reset session
        sessionService.resetSession(user.getId().toHexString());

        //new session
        String userId = user.getId().toHexString();
        String name = user.getFullName();
        String token =  tokenService.generateToken(userId, name, user.getRole());
        sessionService.saveSession(user.getId().toHexString(), token);

        verifyEmailOrPhoneNumber(user, loginRequest.getAuthMethod());
        updateAuthMeans(user, loginRequest.getAuthMethod());

        // update user with verified email or phone and auth means
        userService.updateUser(user.getId().toHexString(), user);
        return token;
    }

    /**
     * Verifies the digest submitted by the user by computing the digest from the challenge.
     * @param loginRequest the request containing the email and submitted digest
     * @return true if the digest is valid, false otherwise
     */
    private boolean verifyDigest(User user, LoginRequest loginRequest, AbstractAuthProvider authProvider) throws Exception {
        String submittedDigest = loginRequest.getDigest();
        String computedDigest = computeDigest(user, authProvider);
        return submittedDigest.equals(computedDigest);
    }

    /**
     * Generates a new access token using the refresh token.
     * @param token the refresh token to be used
     */
    public String getRefreshToken(String token) {
        // TODO : finish refresh token
        token = formatRawToken(token);
        String userId = tokenService.extractUserId(token);
        User user = userService.getUserById(userId, false);
        String name = user.getFirstName() + " " + user.getName();

        // To change
        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(token)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        return tokenService.generateToken(userId, name, user.getRole());
    }

    /**
     * Logs out the user by deleting the session associated with the token.
     *
     * @param token the token to be invalidated
     */
    public void logout(String token) {
        String userId = tokenService.extractUserId(token);
        sessionService.deleteSession(userId);
    }


    /**
     * Checks if the session is valid by verifying the token.
     *
     * @param token the token to check
     * @return true if the session is valid, false otherwise
     */
    public boolean checkSession(String token) {
        token = formatRawToken(token);
        return isValidSession(token);
    }

    /**
     * Checks if the user is authenticated by verifying the token in the request header.
     *
     * @param request the HTTP request containing the token
     * @return the token if valid (without "Bearer " prefix)
     */
    public String checkUserIsAuthenticated(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        token = formatRawToken(token);
        if (!isValidSession(token)) {
            throw new UnauthorizedException();
        }
        return token;
    }

    /**
     * Updates the auth means of the user after successful authentication.
     */
    private void updateAuthMeans(User user, AuthMethod authMethod) {
        Map<AuthMethod, AuthMean> authMeans = user.getAuthMeans();
        int counter = authMeans.get(authMethod).getCounter();
        authMeans.get(authMethod).setEnabled(true);
        authMeans.get(authMethod).setCounter(++counter);
        authMeans.get(authMethod).setLastLogin(new java.util.Date());
        user.setAuthMeans(authMeans);
    }

    /**
     * Verifies the email or phone number of the user based on the auth method used.
     */
    private void verifyEmailOrPhoneNumber(User user, AuthMethod authMethod) {
        switch (authMethod) {
            case EMAIL:
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                }
                break;
            case SMS:
                if (!user.isPhoneVerified()) {
                    user.setPhoneVerified(true);
                }
                break;
        }
    }

    /**
     * Checks if the session is valid
     *
     * @param token the token to check
     * @return true if the session is valid, false otherwise
     */
    private boolean isValidSession(String token) {
        try {
            String userId = tokenService.extractUserId(token);
            String session = sessionService.getSession(userId);
            if (session == null) {
                throw new IllegalArgumentException("Session not found");
            }
            return tokenService.isValidToken(token);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Formats the raw token by removing the "Bearer " prefix.
     * @param token the raw token to format
     */
    private String formatRawToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token is not in a valid format");
        }
        return token.substring(7); // Deletes "Bearer "
    }

    /**
     * Computes the digest using the challenge and the user's pin code.
     * @param user the user to check the challenge
     * @param authProvider the auth method used
     * @return the user's digest challenge + pin
     */
    private String computeDigest(User user, AbstractAuthProvider authProvider) throws Exception {
        String challenge = authProvider.getChallenge(user);
        String pinCode = user.decryptPinCode();
        try {
            String combined = challenge + pinCode;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Sends an authentication event to the RabbitMQ queue.
     */
    public void sendAuthToQueue(String userId, AuthMethod authMethod, String sourceIp, String userAgent, String status, String failureReason) {
        AuthEventPayload payload = new AuthEventPayload(
                userId,
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                authMethod.getName(),
                status,
                sourceIp,
                userAgent,
                detectApplicationType(userAgent),
                failureReason
        );
        producer.sendAuthLog(payload);
    }

    /**
     * Detects the application type based on the user agent string.
     * @param userAgent the user agent string
     * @return the application type (e.g., "WebApp", "MobileApp", "Unknown")
     */
    private String detectApplicationType(String userAgent) {
        if (userAgent == null) return "Unknown";

        String ua = userAgent.toLowerCase();

        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad")) {
            return "MobileApp";
        } else if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) {
            return "WebApp";
        } else {
            return "Unknown";
        }
    }

    public String getUserIdFromEmail(String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user.getId().toHexString();
    }
}
