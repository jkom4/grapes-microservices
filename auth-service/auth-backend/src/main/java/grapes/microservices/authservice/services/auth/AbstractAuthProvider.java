package grapes.microservices.authservice.services.auth;

import java.io.IOException;
import java.security.SecureRandom;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.ChallengeService;
import grapes.microservices.authservice.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Abstract class for authentication providers
 * Contains the basic methods for authentication and challenge verification
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public abstract class AbstractAuthProvider {

    @Autowired
    protected SessionService sessionService;

    @Autowired
    protected ChallengeService challengeService;

    /**
     * Sends a challenge to the user to verify their identity
     * @param user the user to send the challenge to
     * @return true if the challenge was sent successfully, false otherwise
     */
    public abstract boolean sendChallenge(User user) throws IOException;

    /**
     * Verifies the challenge submitted by the user
     * @param user the email of the user
     * @param submittedChallenge the challenge submitted by the user
     * @return true if the challenge is correct, false otherwise
     */
    public abstract boolean verifyChallenge(User user, String submittedChallenge);

    /**
     * Processes the challenge submitted by the user
     * If the challenge is correct, a token is generated
     * @param user the user that submitted the challenge
     * @param submittedChallenge the challenge submitted by the user
     * @return the generated token
     */
    public abstract String processChallenge(User user, String submittedChallenge);

    /**
     * Generates a random challenge
     * @return the generated challenge
     */
    protected String generateChallenge() {
        SecureRandom random = new SecureRandom();
        int challenge = random.nextInt(999999);
        return String.format("%06d", challenge);
    }
}