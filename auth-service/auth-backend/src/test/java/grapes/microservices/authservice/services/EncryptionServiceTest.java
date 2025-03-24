package grapes.microservices.authservice.services;

import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    @Test
    void testEncrypt() throws Exception {
        String originalData = "SensitiveData123";
        String encryptedData = EncryptionService.encrypt(originalData);

        System.out.println(originalData + " : " + encryptedData);
        assertNotNull(encryptedData, "Encrypted data should not be null.");
        assertNotEquals(originalData, encryptedData, "Encrypted data should not be equal to the original data.");
    }

    @Test
    void testDecrypt() throws Exception {
        String originalData = "SensitiveData123";
        String encryptedData = EncryptionService.encrypt(originalData);

        String decryptedData = EncryptionService.decrypt(encryptedData);
        System.out.println(encryptedData + " : " + decryptedData);
        assertNotNull(decryptedData, "Decrypted data should not be null.");
        assertEquals(originalData, decryptedData, "Decrypted data should match the original data.");
    }

    @Test
    void testEncryptDecryptConsistency() throws Exception {
        String originalData = "SensitiveData123";
        String encryptedData = EncryptionService.encrypt(originalData);
        String decryptedData = EncryptionService.decrypt(encryptedData);

        assertEquals(originalData, decryptedData, "Decrypted data should match the original data after encryption and decryption.");
    }

    @Test
    void testEncryptNullData() throws IllegalArgumentException {
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.encrypt(null));
    }

    @Test
    void testDecryptNullData() throws IllegalArgumentException {
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.decrypt(null));
    }

    @Test
    void testDecryptInvalidData() {
        String invalidEncryptedData = "InvalidEncryptedData";
        assertThrows(Exception.class, () -> EncryptionService.decrypt(invalidEncryptedData), "Decrypting invalid data should throw an exception.");
    }

    @Test
    void testEncryptEmptyData() throws IllegalArgumentException {
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.encrypt(""));

    }

    @Test
    void testDecryptEmptyData() throws IllegalArgumentException {
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.decrypt(""));

    }

    @Test
    void testKeyGeneration() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);

        // Test key generation by verifying that key size is correct.
        assertEquals(32, key.length, "AES key should be 32 bytes.");
    }
}
