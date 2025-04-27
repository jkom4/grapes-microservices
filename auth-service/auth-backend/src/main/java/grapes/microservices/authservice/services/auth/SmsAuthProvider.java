package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.SmsService;
import grapes.microservices.authservice.utils.challenge_request_limiter.OneCallPerMinutePerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SmsAuthProvider is an authentication provider that sends challenges to users via SMS.
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class SmsAuthProvider extends AbstractAuthProvider {

    @Autowired
    private final SmsService smsService;

    @Override
    @OneCallPerMinutePerUser
    public String sendChallenge(User user) {
        String challenge = generateChallenge();
        challengeService.saveChallengeForUser(user.getPhoneNumber(), challenge);
        String message = "Please use the following code to authenticate: " + challenge;
        smsService.sendSms(user.getPhoneNumber(), message);
        return challenge;
    }

    @Override
    public String getChallenge(User user) throws Exception {
        try {
            ChallengeWithTimestamp storedChallenge = challengeService.getChallengeForUser(user.getPhoneNumber());
            challengeService.verifyChallengeAuthenticity(storedChallenge);
            return storedChallenge.getChallenge();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void deleteChallenge(User user) {
        try {
            challengeService.evictChallengeCache(user.getPhoneNumber());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete challenge: " + e.getMessage());
        }
    }
}
