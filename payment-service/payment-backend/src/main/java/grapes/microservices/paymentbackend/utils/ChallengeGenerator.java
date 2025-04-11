package grapes.microservices.paymentbackend.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitaire pour générer des challenges uniques pour l'authentification
 */
public class ChallengeGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère un challenge aléatoire de 32 octets encodé en Base64
     * @return Le challenge généré
     */
    public static String generateChallenge() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}