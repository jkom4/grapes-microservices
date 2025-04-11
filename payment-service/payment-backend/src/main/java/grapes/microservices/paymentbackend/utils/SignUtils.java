package grapes.microservices.paymentbackend.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignUtils {

    /**
     * Sign data with private key
     * @param data the data to sign
     * @param privateKey the private key
     * @return the signed message
     */
    public static String signData(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * Verify signature with public key
     * @param data the data to verify
     * @param signature the signature to verify
     * @param publicKey the public key
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verifySignature(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(Base64.getDecoder().decode(signature));
    }
}