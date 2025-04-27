package grapes.microservices.authservice.services.auth;

import java.io.IOException;
import java.security.SecureRandom;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.ChallengeService;
import grapes.microservices.authservice.services.SessionService;
import grapes.microservices.authservice.services.TokenService;
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
    protected TokenService tokenService;

    @Autowired
    protected ChallengeService challengeService;

    /**
     * Sends a challenge to the user to verify their identity
     * @param user the user to send the challenge to
     * @return the provided challenge
     */
    public abstract String sendChallenge(User user) throws IOException;

    /**
     * Get the challenge submitted to the user
     * @param user the user that submitted the challenge
     */
    public abstract String getChallenge(User user) throws Exception;

    /**
     * Deletes the challenge for the user
     * @param user the user to delete the challenge for
     */
    public abstract void deleteChallenge(User user);

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