package grapes.microservices.authservice.services.auth;

import java.io.IOException;
import java.security.SecureRandom;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.ChallengeService;
import grapes.microservices.authservice.services.EmailService;
import grapes.microservices.authservice.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Abstract class for authentication providers
 * Contains the basic methods for authentication and challenge verification
 */
@Service
@RequiredArgsConstructor
public abstract class AbstractAuthProvider {

    @Autowired
    private TokenService tokenService;

    @Autowired
    protected ChallengeService challengeService;

    /**
     * Sends a challenge to the user to verify their identity
     * @param user the user to send the challenge to
     */
    public abstract void sendChallenge(User user) throws IOException;

    /**
     * Verifies the challenge submitted by the user
     * @param email the email of the user
     * @param submittedChallenge the challenge submitted by the user
     * @return true if the challenge is correct, false otherwise
     */
    public boolean verifyChallenge(String email, String submittedChallenge) {
        String storedChallenge = challengeService.getChallengeForUser(email);

        if (storedChallenge == null) {
            throw new RuntimeException("Challenge not found.");
        }
        if (storedChallenge.equals(submittedChallenge)) {
            return true;
        }
        throw new RuntimeException("Challenge does not match");
    }

    /**
     * Processes the challenge submitted by the user
     * If the challenge is correct, a token is generated
     * @param user the user that submitted the challenge
     * @param submittedChallenge the challenge submitted by the user
     * @return the generated token
     */
    public String processChallenge(User user, String submittedChallenge) {
        boolean isValid = verifyChallenge(user.getEmail(), submittedChallenge);
        if (isValid) {
            return tokenService.generateToken(user.getEmail());
        }
        throw new RuntimeException("The challenge is not valid.");
    }

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