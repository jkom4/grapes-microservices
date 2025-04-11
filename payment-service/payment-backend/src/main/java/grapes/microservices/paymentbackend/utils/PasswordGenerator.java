package grapes.microservices.paymentbackend.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utilitaire pour générer des mots de passe hachés pour les tests
 */
public class PasswordGenerator {

    private static final String SALT = "HIARD";

    public static void main(String[] args) {
        String password = "P@ssw0rd"; // Mot de passe à hasher
        String hashedPassword = generateHashedPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Salted and Hashed Password: " + hashedPassword);
    }

    public static String generateHashedPassword(String password) {
        try {
            // Saler le mot de passe
            String saltedPassword = password + SALT;

            // Hasher le mot de passe
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(saltedPassword.getBytes());

            // Encoder en Base64
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}