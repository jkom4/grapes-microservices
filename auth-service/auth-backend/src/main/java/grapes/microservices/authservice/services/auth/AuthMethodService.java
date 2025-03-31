package grapes.microservices.authservice.services.auth;

import grapes.microservices.authservice.models.AuthMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthMethodService {

    @Autowired
    private EmailAuthProvider emailAuthProvider;
    //need to add others here later

    /**
     * Get the authentication provider for the given authentication method
     * @param authMethod the authentication method
     * @return the authentication provider
     */
    public AbstractAuthProvider getAuthProvider(AuthMethod authMethod) {
        return switch (authMethod) {
            case EMAIL -> emailAuthProvider;
            default -> throw new IllegalArgumentException("Invalid auth method");
        };
    }
}
