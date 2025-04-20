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
     * Generates an AES-256 key from a password and a salt.
     * @param password The password to derive the key from.
     * @param salt The salt to use for key derivation.
     * @return The generated SecretKey.
     * @throws Exception If key generation fails.
     */
    private static SecretKey generateKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * Encrypts plaintext using AES-256.
     * The challenge is used as the salt for key generation.
     * A random IV is generated and prepended to the ciphertext.
     * @param plaintext The text to encrypt.
     * @param challenge The challenge string (used as salt).
     * @param pin The PIN or password used for key generation.
     * @return Base64 encoded string containing IV + ciphertext.
     * @throws Exception If encryption fails.
     */
    public static String encrypt(String plaintext, String challenge, String pin) throws Exception {
        // Use the challenge as salt
        byte[] salt = challenge.getBytes(StandardCharsets.UTF_8);

        // Generate the key
        SecretKey key = generateKey(pin, salt);

        // Generate a random IV
        byte[] iv = new byte[16]; // AES block size for CBC
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        // Encrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext: IV (16 bytes) + Ciphertext
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts text using AES-256.
     * Expects the input string to be Base64 encoded IV + ciphertext.
     * The challenge is used as the salt for key generation.
     * @param encryptedData Base64 encoded string (IV + ciphertext).
     * @param challenge The challenge string (used as salt).
     * @param pin The PIN or password used for key generation.
     * @return The original plaintext.
     * @throws Exception If decryption fails (e.g., bad padding, wrong key).
     */
    public static String decrypt(String encryptedData, String challenge, String pin) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        // Extract IV (first 16 bytes)
        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        // Generate the key
        byte[] salt = challenge.getBytes(StandardCharsets.UTF_8);
        SecretKey key = generateKey(pin, salt);

        // Decrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Generates an encrypted response to the challenge.
     * Encrypts the challenge itself using the challenge as salt and the pin as password.
     * @param challenge The challenge string to encrypt and use as salt.
     * @param pin The PIN or password.
     * @return The encrypted challenge response.
     * @throws Exception If encryption fails.
     */
    public static String generateChallengeResponse(String challenge, String pin) throws Exception {
        // The response encrypts the original challenge
        return encrypt(challenge, challenge, pin);
    }

    /**
     * Verifies a response to a challenge.
     * Decrypts the response and checks if it matches the original challenge.
     * @param response The encrypted response received.
     * @param challenge The original challenge string.
     * @param pin The PIN or password.
     * @return true if the decrypted response matches the challenge, false otherwise.
     */
    public static boolean validateChallengeResponse(String response, String challenge, String pin) {
        try {
            String decrypted = decrypt(response, challenge, pin);
            return challenge.equals(decrypted);
        } catch (Exception e) {
            // It's good practice to log the exception here if possible
            // e.g., log.warn("Challenge validation failed due to decryption error for challenge '{}'", challenge, e);
            return false; // If decryption fails for any reason, the response is invalid.
        }
    }
}