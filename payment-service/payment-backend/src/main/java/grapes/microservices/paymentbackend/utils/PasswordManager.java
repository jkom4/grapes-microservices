package grapes.microservices.paymentbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class to manage passwords
 */
@Component
public class PasswordManager {

    @Value("${password.salt:HIARD}")
    private String SALT;

    public String saltPassword(String password) {
        return password + SALT;
    }

    /**
     * Generate a random salt
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hash a password using SHA-256
     * @param password the password to hash
     * @return the hashed password as a Base64 string
     */
    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashedBytes = digest.digest(password.getBytes());

        return Base64.getEncoder().encodeToString(hashedBytes);
    }
}