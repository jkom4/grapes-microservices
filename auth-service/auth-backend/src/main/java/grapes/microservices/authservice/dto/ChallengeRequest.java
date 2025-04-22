package grapes.microservices.authservice.dto;

import grapes.microservices.authservice.models.AuthMethod;
import lombok.Data;

@Data
public class ChallengeRequest {

    private String email;
    private String password;
    private AuthMethod authMethod;
}
