package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailAuthProvider extends AbstractAuthProvider{

    @Autowired
    private final EmailService emailService;

    @Override
    public void sendChallenge(User user) throws IOException {
        String challenge = generateChallenge();
        challengeService.saveChallengeForUser(user.getEmail(), challenge);
        String message = "Please use the following code to authenticate: " + challenge;
        emailService.sendMail(user.getEmail(), "Authentication challenge", message);
    }
}
