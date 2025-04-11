package grapes.microservices.paymentbackend.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class KeystoreUtils {

    /**
     * Get the private key from the keystore
     * @param keystorePath path to the keystore
     * @param keystorePassword password of the keystore
     * @param alias alias of the key
     * @param keyPassword password of the key
     * @return the private key
     */
    public static PrivateKey getPrivateKey(String keystorePath, String keystorePassword, String alias, String keyPassword) throws Exception {
        FileInputStream fis = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fis, keystorePassword.toCharArray());
        return (PrivateKey) keystore.getKey(alias, keyPassword.toCharArray());
    }

    /**
     * Get the certificate from the keystore
     * @param keystorePath path to the keystore
     * @param keystorePassword password of the keystore
     * @param alias alias of the certificate
     * @return the certificate
     */
    public static Certificate getCertificate(String keystorePath, String keystorePassword, String alias) throws Exception {
        FileInputStream fis = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fis, keystorePassword.toCharArray());
        return keystore.getCertificate(alias);
    }
}