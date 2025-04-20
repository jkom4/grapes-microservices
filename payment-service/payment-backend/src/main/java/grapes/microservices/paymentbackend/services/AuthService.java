package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

/**
 * Service handling authentication token operations for two-factor authentication.
 * Manages OTP generation, storage, delivery, and verification for secure payments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;


    public String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }


    @Transactional
    public AuthToken createToken(Client client) {
        String otp = generateOtp();
        // Use AuthToken constructor that initializes dates and isUsed flag
        AuthToken token = new AuthToken(otp, client);

        log.info("Generated OTP token for client {}: {}", client.getEmail(), otp);

        // Send OTP via SMS
        if (client.getPhoneNumber() != null && !client.getPhoneNumber().isEmpty()) {
            try {
                smsService.sendOtp(client.getPhoneNumber(), otp);
                log.info("OTP sent to phone number: {}", client.getPhoneNumber());
            } catch (RuntimeException e) {
                log.error("Failed to send OTP SMS to client {}: {}", client.getEmail(), e.getMessage());
                throw new IllegalStateException("Failed to send OTP via SMS", e);
            }
        } else {
            log.error("Client {} doesn't have a registered phone number", client.getEmail());
            throw new IllegalStateException("Client doesn't have a registered phone number");
        }

        // Save the token to the database
        return tokenRepository.save(token);
    }


    @Transactional
    public AuthToken saveToken(AuthToken token) {
        log.info("Saving token {} for client {}", token.getToken(), token.getClient().getEmail());
        return tokenRepository.save(token);
    }


    @Transactional
    public boolean verifyToken(String tokenValue, Client client) {
        log.info("Verifying token {} for client {}", tokenValue, client.getEmail());

        Optional<AuthToken> tokenOpt = tokenRepository.findByTokenAndClient(tokenValue, client);

        if (tokenOpt.isEmpty()) {
            log.warn("Token '{}' not found for client {}", tokenValue, client.getEmail());
            return false;
        }

        AuthToken token = tokenOpt.get();

        if (!token.isValid()) {
            log.warn("Token '{}' is expired or already used for client {}", tokenValue, client.getEmail());
            return false;
        }

        // Mark token as used to prevent replay attacks
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("Token '{}' verified successfully and marked as used for client {}", tokenValue, client.getEmail());

        return true;
    }


    public Optional<AuthToken> getLastToken(Client client) {
        return tokenRepository.findFirstByClientOrderByCreatedAtDesc(client);
    }
}