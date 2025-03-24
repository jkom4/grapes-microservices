package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.security.AESConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncryptionService provides AES-256 encryption using GCM mode to securely encrypt sensitive user data.
 * This class is designed to encrypt fields such as National ID, Card Number, and PIN Code.
 * - Uses AES-256 with GCM mode for authenticated encryption.
 * - Generates a random IV (Initialization Vector) for each encryption.
 * - Stores encrypted data as a Base64-encoded string.
 *
 * @author Cameron
 */
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length (in bits)
    private static final int IV_LENGTH = 12; // Recommended IV size for GCM mode

    private final SecretKey secretKey;

    /**
     * Constructs an EncryptionService instance with a given AES key.
     */
    @Autowired
    public EncryptionService() {
        this.secretKey = new SecretKeySpec(AESConfig.getKey(), AES_ALGORITHM);
    }

    /**
     * Generates a new AES-256 key.
     *
     * @return A byte array containing the AES-256 key.
     * @throws Exception If key generation fails.
     */
    public static byte[] generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(256); // 256-bit key
        return keyGenerator.generateKey().getEncoded();
    }

    /**
     * Encrypts a given string using AES-256 GCM mode.
     *
     * @param data The plaintext data to encrypt.
     * @return The encrypted data as a Base64-encoded string.
     * @throws Exception If encryption fails.
     */
    private String encrypt(String data) throws Exception {
        if (data == null || data.isEmpty()) {
            return null;
        }
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generate a random IV
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, IV_LENGTH, encryptedData.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    /**
     * Encrypts sensitive user data fields such as National ID, Card Number, and PIN Code.
     *
     * @param user The user object containing the data to be encrypted.
     */
    public void encryptData(User user) {
        try {
            user.setNationalId(encrypt(user.getNationalId()));
            user.setCardNumber(encrypt(user.getCardNumber()));
            user.setPinCode(encrypt(user.getPinCode()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting user data", e);
        }
    }
}
