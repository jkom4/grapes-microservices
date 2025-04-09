package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.AuthMethod;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for getting the authentication provider for a given authentication method
 * Used to abstract the process of getting the authentication provider
 * @author Cameron
 */
@Service
@AllArgsConstructor
public class AuthMethodService {

    @Autowired
    private EmailAuthProvider emailAuthProvider;

    @Autowired
    private SmsAuthProvider smsAuthProvider;
    //need to add others here later

    /**
     * Get the authentication provider for the given authentication method
     * @param authMethod the authentication method
     * @return the authentication provider
     */
    public AbstractAuthProvider getAuthProvider(AuthMethod authMethod) {
        return switch (authMethod) {
            case EMAIL -> emailAuthProvider;
            case SMS -> smsAuthProvider;
            default -> throw new IllegalArgumentException("Invalid auth method");
        };
    }
}
