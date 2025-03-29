package grapes.microservices.authservice.services.auth;

import java.util.UUID;
import grapes.microservices.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public abstract class AbstractAuthProvider {



    @Autowired
    private final UserService userService;


    public abstract void authenticate(String identifier, String credential);

    /**
     * Generates a random challenge
     * @return the generated challenge
     */
    protected String generateChallenge() {
        return UUID.randomUUID().toString();
    }
}