package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.EmailService;
import grapes.microservices.authservice.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * EmailAuthProvider is an authentication provider that sends challenges to users via email.
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class EmailAuthProvider extends AbstractAuthProvider{

    @Autowired
    private final EmailService emailService;

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean sendChallenge(User user) throws IOException {
        String challenge = generateChallenge();
        challengeService.saveChallengeForUser(user.getEmail(), challenge);
        String message = "Please use the following code to authenticate: " + challenge;
        return emailService.sendMail(user.getEmail(), "Authentication challenge", message);
    }

    @Override
    public boolean verifyChallenge(User user, String submittedChallenge) {
        ChallengeWithTimestamp storedChallenge = challengeService.getChallengeForUser(user.getEmail());
        if (storedChallenge == null) {
            throw new RuntimeException("Challenge not found.");
        }
        if (storedChallenge.isExpired()) {
            challengeService.evictChallengeCache(user.getEmail());
            throw new RuntimeException("Challenge has expired.");
        }
        if (storedChallenge.getChallenge().equals(submittedChallenge)) {
            return true;
        }
        throw new RuntimeException("Challenge does not match");
    }

    @Override
    public String processChallenge(User user, String submittedChallenge) {
        boolean isValid = verifyChallenge(user, submittedChallenge);
        if (isValid) {
            challengeService.evictChallengeCache(user.getEmail());
            String token = tokenService.generateToken(user.getId().toHexString());
            sessionService.saveSession(user.getId().toHexString(), token);
            return token;
        }
        throw new RuntimeException("The challenge is not valid.");
    }
}
