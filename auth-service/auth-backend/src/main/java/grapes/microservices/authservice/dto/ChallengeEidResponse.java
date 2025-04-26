package grapes.microservices.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ChallengeEidResponse {
    private String message;
    private String challenge;
}
