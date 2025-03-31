package grapes.microservices.authservice.services;

import grapes.microservices.authservice.security.AESConfig;
import grapes.microservices.authservice.utils.AuthLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Static utility class for AES-256 encryption and decryption using GCM mode.
 * This class provides methods to securely encrypt and decrypt sensitive user data.
 *
 * @author Cameron
 */
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final SecretKey secretKey;

    static {
        try {
            byte[] key = AESConfig.getKey();
            if (key == null) {
                key = AESConfig.generateAESKey();
                logger.warn("AES key is generated as it was not provided.");
            } else {
                logger.info("AES key loaded from configuration.");
            }
            secretKey = new SecretKeySpec(key, AES_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EncryptionService", e);
        }
    }

    private EncryptionService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Encrypts a given plaintext string using AES-256 GCM mode.
     *
     * @param data The plaintext data to encrypt.
     * @return The encrypted data as a Base64-encoded string, or {@code null} if the input is null or empty.
     * @throws Exception If encryption fails due to cryptographic errors.
     */
    public static String encrypt(String data) throws Exception {
        if (data == null || data.isEmpty()) {
            logger.warn("Data to encrypt is null or empty.");
            throw new IllegalArgumentException("Data to encrypt is null or empty.");
        }
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, IV_LENGTH, encryptedData.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    /**
     * Decrypts an AES-256 GCM encrypted string.
     *
     * @param encryptedData The Base64-encoded encrypted data containing the IV and ciphertext.
     * @return The decrypted plaintext string, or {@code null} if the input is invalid.
     * @throws Exception If decryption fails due to an invalid key, corrupted data, or authentication failure.
     */
    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            logger.warn("Data to decrypt is null or empty.");
            throw new IllegalArgumentException("Data to decrypt is null or empty.");
        }
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);
        byte[] encryptedBytes = new byte[decodedData.length - IV_LENGTH];
        System.arraycopy(decodedData, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}