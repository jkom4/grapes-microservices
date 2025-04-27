package grapes.microservices.paymentbackend.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Utility for accessing keys and certificates from keystores.
 * Provides methods to retrieve private keys and certificates for cryptographic operations.
 */
public class KeystoreUtils {

    /**
     * Retrieves a private key from a Java KeyStore file.
     *
     * @param keystorePath Path to the keystore file
     * @param keystorePassword Password to access the keystore
     * @param alias Alias identifying the key in the keystore
     * @param keyPassword Password to access the specific key
     * @return The private key
     * @throws Exception If the key cannot be retrieved
     */
    public static PrivateKey getPrivateKey(String keystorePath, String keystorePassword, String alias, String keyPassword) throws Exception {
        FileInputStream fis = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fis, keystorePassword.toCharArray());
        return (PrivateKey) keystore.getKey(alias, keyPassword.toCharArray());
    }

    /**
     * Retrieves a certificate from a Java KeyStore file.
     *
     * @param keystorePath Path to the keystore file
     * @param keystorePassword Password to access the keystore
     * @param alias Alias identifying the certificate in the keystore
     * @return The certificate
     * @throws Exception If the certificate cannot be retrieved
     */
    public static Certificate getCertificate(String keystorePath, String keystorePassword, String alias) throws Exception {
        FileInputStream fis = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fis, keystorePassword.toCharArray());
        return keystore.getCertificate(alias);
    }
}