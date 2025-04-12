package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import grapes.microservices.authservice.services.EidCardService;

import javax.crypto.Cipher;

/**
 * Authentication provider using Belgian eID smart-card.
 * Implements challenge-response logic using the eID private key for signature.
 *
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class EidAuthProvider extends AbstractAuthProvider {

    private final EidCardService eidCardService;

    @Override
    public boolean sendChallenge(User user) {
        try {
            // 1. Generate a challenge
            String challenge = generateChallenge();
            challengeService.saveChallengeForUser(user.getNationalId(), challenge);

            // 2. Encrypt the challenge using the user's public key from eID
            X509Certificate cert = eidCardService.getCertificateFromUser(user); // public key cert
            PublicKey publicKey = cert.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedChallenge = cipher.doFinal(challenge.getBytes());

            // 3. Send encrypted challenge to the client
            // For now, just log or return it in dev (in prod, send via secure channel)
            System.out.println("Encrypted challenge (Base64): " + Base64.getEncoder().encodeToString(encryptedChallenge));
            return true;
        } catch (Exception e) {
            System.err.println("Error during challenge generation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyChallenge(User user, String submittedSignatureBase64) {
        try {
            // 1. Retrieve original challenge
            ChallengeWithTimestamp originalChallenge = challengeService.getChallengeForUser(user.getNationalId());

            // 2. Reconstruct the message (challenge + PIN if applicable)
            String expectedMessage = originalChallenge + "1234"; // Replace if PIN is securely passed
            byte[] expectedBytes = expectedMessage.getBytes();

            // 3. Verify the signature using public key from eID
            X509Certificate cert = eidCardService.getCertificateFromUser(user);
            Signature signature = Signature.getInstance("SHA384withECDSA");
            signature.initVerify(cert);
            signature.update(expectedBytes);

            byte[] submittedSignature = Base64.getDecoder().decode(submittedSignatureBase64);
            return signature.verify(submittedSignature);
        } catch (Exception e) {
            System.err.println("Challenge verification failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String processChallenge(User user, String submittedSignature) {
        if (verifyChallenge(user, submittedSignature)) {
            return tokenService.generateToken(user.getId().toHexString());
        }
        return null;
    }
}
