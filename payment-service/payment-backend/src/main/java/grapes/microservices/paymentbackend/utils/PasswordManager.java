package grapes.microservices.paymentbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for secure password management.
 * Provides methods for salting, hashing, and verifying passwords.
 */
@Component
public class PasswordManager {

    @Value("${password.salt:HIARD}")
    private String SALT;

    /**
     * Adds salt to a password before hashing.
     *
     * @param password The password to salt
     * @return The salted password
     */
    public String saltPassword(String password) {
        return password + SALT;
    }

    /**
     * Generates a random salt for password hashing.
     *
     * @return The salt as a Base64 encoded string
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hashes a password using SHA-256 algorithm.
     *
     * @param password The password to hash
     * @return The hashed password as a Base64 encoded string
     * @throws NoSuchAlgorithmException If SHA-256 algorithm is not available
     */
    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * Verifies if a raw password matches a stored hashed password.
     *
     * @param rawPassword The raw password to verify
     * @param encodedPassword The stored encoded password
     * @return true if the passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            String saltedRawPassword = saltPassword(rawPassword);
            String hashedRawPassword = hashPassword(saltedRawPassword);
            return hashedRawPassword.equals(encodedPassword);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}