package grapes.microservices.paymentbackend.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * AesCryptoUtils.java
 *
 * This class provides utility methods for AES-256 encryption and decryption.
 * It uses PBKDF2 with HmacSHA256 for key generation and CBC mode with PKCS5Padding for encryption.
 *
 */

public class AesCryptoUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;

    /**
     * Génère une clé AES-256 à partir d'un mot de passe et d'un salt
     */
    private static SecretKey generateKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * Chiffre un texte avec AES-256
     */
    public static String encrypt(String plaintext, String challenge, String pin) throws Exception {
        // Utiliser le challenge comme salt
        byte[] salt = challenge.getBytes(StandardCharsets.UTF_8);

        // Générer la clé
        SecretKey key = generateKey(pin, salt);

        // Générer un IV aléatoire
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        // Chiffrer
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combiner IV et texte chiffré
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Déchiffre un texte avec AES-256
     */
    public static String decrypt(String encryptedData, String challenge, String pin) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        // Extraire l'IV
        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        // Générer la clé
        byte[] salt = challenge.getBytes(StandardCharsets.UTF_8);
        SecretKey key = generateKey(pin, salt);

        // Déchiffrer
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Génère une réponse chiffrée au challenge
     */
    public static String generateChallengeResponse(String challenge, String pin) throws Exception {
        return encrypt(challenge, challenge, pin);
    }

    /**
     * Vérifie une réponse à un challenge
     */
    public static boolean validateChallengeResponse(String response, String challenge, String pin) {
        try {
            String decrypted = decrypt(response, challenge, pin);
            return challenge.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }
}