package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.EmailService;
import grapes.microservices.authservice.utils.challenge_request_limiter.OneCallPerMinutePerUser;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * EmailAuthProvider is an authentication provider that sends challenges to users via email.
 *
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class EmailAuthProvider extends AbstractAuthProvider {

    @Autowired
    private EmailService emailService;

    @Override
    @OneCallPerMinutePerUser
    public boolean sendChallenge(User user) throws IOException {
        String challenge = generateChallenge();
        challengeService.saveChallengeForUser(user.getEmail(), challenge);
        String message = "Please use the following code to authenticate: " + challenge;
        return emailService.sendMail(user.getEmail(), "Authentication challenge", message);
    }

    @Override
    public String getChallenge(User user) throws Exception {
        try {
            ChallengeWithTimestamp storedChallenge = challengeService.getChallengeForUser(user.getEmail());
            challengeService.verifyChallengeAuthenticity(storedChallenge);
            return storedChallenge.getChallenge();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void deleteChallenge(User user) {
        try {
            challengeService.evictChallengeCache(user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete challenge: " + e.getMessage());
        }
    }
}
