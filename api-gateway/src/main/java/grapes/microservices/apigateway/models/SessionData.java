package grapes.microservices.apigateway.models;

import lombok.Data;

@Data
public class SessionData {
    private String accessToken;
    private String refreshToken;
    private long expiresAt; // timestamp

}
