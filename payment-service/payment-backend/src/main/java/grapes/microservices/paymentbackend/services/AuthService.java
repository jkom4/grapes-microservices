package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    /**
     * Generate a 6-digit OTP
     * @return the OTP
     */
    public String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Create and store an authentication token for a user
     * @param user the user
     * @return the created token
     */
    public AuthToken createToken(User user) {
        String otp = generateOtp();
        AuthToken token = new AuthToken(otp, user);

        log.info("Generated OTP token for user {}: {}", user.getLogin(), otp);

        // Send OTP via SMS
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            smsService.sendOtp(user.getPhoneNumber(), otp);
            log.info("OTP sent to phone number: {}", user.getPhoneNumber());
        } else {
            log.error("User {} doesn't have a registered phone number", user.getLogin());
            throw new IllegalStateException("User doesn't have a registered phone number");
        }

        return tokenRepository.save(token);
    }

    /**
     * Save a token to the database
     * @param token the token to save
     * @return the saved token
     */
    public AuthToken saveToken(AuthToken token) {
        log.info("Saving token {} for user {}", token.getToken(), token.getUser().getLogin());
        return tokenRepository.save(token);
    }

    /**
     * Verify if a token is valid
     * @param tokenValue the token value
     * @param user the user
     * @return true if valid, false otherwise
     */
    public boolean verifyToken(String tokenValue, User user) {
        log.info("Verifying token {} for user {}", tokenValue, user.getLogin());

        Optional<AuthToken> tokenOpt = tokenRepository.findByTokenAndUser(tokenValue, user);

        if (tokenOpt.isEmpty()) {
            log.warn("Token not found for user {}", user.getLogin());
            return false;
        }

        AuthToken token = tokenOpt.get();

        if (!token.isValid()) {
            log.warn("Token is expired or already used for user {}", user.getLogin());
            return false;
        }

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("Token verified successfully for user {}", user.getLogin());

        return true;
    }

    /**
     * Get the last generated token for a user
     * @param user the user
     * @return the token if found, empty otherwise
     */
    public Optional<AuthToken> getLastToken(User user) {
        return tokenRepository.findFirstByUserOrderByCreatedAtDesc(user);
    }
}