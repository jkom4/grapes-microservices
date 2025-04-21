package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.ChallengeService;
import grapes.microservices.authservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
public class EIDVerificationService {

    @Autowired
    ChallengeService challengeService;

    @Autowired
    EIDSignatureService eidSignatureService;

    @Autowired
    UserService userService;

    /**
     * Verifies a signed challenge signature via eID card.
     *
     * @param email The user's email
     * @param base64Signature The base64-encoded signature of challenge + PIN
     * @param pin The PIN used in the concatenation (not sensitive here, just for verification)
     * @return true if the signature is valid, false otherwise
     * @throws Exception if an error occurs
     */
    public boolean verifyEIDChallenge(String email, String base64Signature, String pin) throws Exception {
        var challengeObj = challengeService.getChallengeForUser(email);
        if (challengeObj == null || challengeObj.isExpired()) {
            throw new IllegalArgumentException("Challenge expired or not found.");
        }

        String challenge = challengeObj.getChallenge();
        String messageToVerify = challenge + pin;

        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        PublicKey publicKey = eidSignatureService.extractPublicKey();

        boolean valid = eidSignatureService.verifySignature(base64Signature, messageToVerify, publicKey);

        if (valid) {
            challengeService.evictChallengeCache(email);
        }

        return valid;
    }
}