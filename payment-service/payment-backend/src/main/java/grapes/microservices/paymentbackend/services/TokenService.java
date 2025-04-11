package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    @Autowired
    public TokenService(AuthTokenRepository tokenRepository, SmsService smsService) {
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
    }

    public AuthToken generateToken(User user) {
        // Generate a random 6-digit code
        String tokenValue = generateRandomCode();

        // Create a token that expires in 3 minutes
        AuthToken token = new AuthToken();
        token.setTokenValue(tokenValue);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        token.setUsed(false);

        // Save the token
        AuthToken savedToken = tokenRepository.save(token);

        // Send SMS with the token
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            smsService.sendSms(user.getPhoneNumber(), "Your verification code is: " + tokenValue);
        }

        return savedToken;
    }

    public boolean verifyToken(String tokenValue) {
        Optional<AuthToken> tokenOpt = tokenRepository.findByTokenValue(tokenValue);

        if (tokenOpt.isPresent()) {
            AuthToken token = tokenOpt.get();

            if (token.isValid()) {
                // Mark token as used
                token.setUsed(true);
                tokenRepository.save(token);
                return true;
            }
        }

        return false;
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}