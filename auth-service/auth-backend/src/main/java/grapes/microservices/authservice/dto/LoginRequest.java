package grapes.microservices.authservice.dto;

import grapes.microservices.authservice.models.AuthMethod;
import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String digest;
    private AuthMethod authMethod;
}