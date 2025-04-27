package grapes.microservices.paymentbackend.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Utility for digital signature operations.
 * Provides methods for signing data and verifying signatures.
 */
public class SignUtils {

    /**
     * Signs data using a private key.
     * Uses SHA256withRSA algorithm for the digital signature.
     *
     * @param data The data to sign
     * @param privateKey The private key to use for signing
     * @return The Base64 encoded signature
     * @throws Exception If signing fails
     */
    public static String signData(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * Verifies a digital signature using a public key.
     *
     * @param data The original data that was signed
     * @param signature The Base64 encoded signature to verify
     * @param publicKey The public key corresponding to the private key used for signing
     * @return true if the signature is valid, false otherwise
     * @throws Exception If verification process fails
     */
    public static boolean verifySignature(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(Base64.getDecoder().decode(signature));
    }
}