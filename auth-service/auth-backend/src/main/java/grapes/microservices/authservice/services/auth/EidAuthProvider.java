package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import grapes.microservices.authservice.services.EIDCardService;

/**
 * Authentication provider using Belgian eID smart-card.
 * Implements challenge-response logic using the eID private key for signature.
 *
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class EidAuthProvider extends AbstractAuthProvider {

    private final EIDCardService eidCardService;

    @Override
    public String sendChallenge(User user) {
        String challenge = generateChallenge();
        challengeService.saveChallengeForUser(user.getNationalId(), challenge);

        // encrypt the challenge using the public key from the eID card
        try {
            return eidCardService.encryptMessage(challenge);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt challenge: " + e.getMessage());
        }
    }

    @Override
    public String getChallenge(User user) throws Exception {
        try {
            ChallengeWithTimestamp storedChallenge = challengeService.getChallengeForUser(user.getNationalId());
            challengeService.verifyChallengeAuthenticity(storedChallenge);
            return storedChallenge.getChallenge();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void deleteChallenge(User user) {
        try {
            challengeService.evictChallengeCache(user.getNationalId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete challenge: " + e.getMessage());
        }
    }
}
