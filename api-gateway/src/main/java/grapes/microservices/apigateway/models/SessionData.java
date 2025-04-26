package grapes.microservices.apigateway.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionData {
    private String accessToken;
    private String refreshToken;
    private long expiresAt; // timestamp
}

