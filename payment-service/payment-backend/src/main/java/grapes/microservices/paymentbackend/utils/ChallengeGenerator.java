package grapes.microservices.paymentbackend.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating unique challenges for authentication processes.
 * Provides methods to create secure random tokens for challenge-response authentication.
 */
public class ChallengeGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a random challenge of 32 bytes encoded in Base64URL format.
     * This creates a cryptographically secure random token for use in
     * authentication challenges.
     *
     * @return The generated challenge string
     */
    public static String generateChallenge() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}