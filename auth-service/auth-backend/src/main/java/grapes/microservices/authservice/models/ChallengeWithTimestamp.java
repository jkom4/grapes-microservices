package grapes.microservices.authservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * A challenge with a timestamp
 * Used to store challenges in the cache
 * The timestamp is used to determine when the challenge was created
 * @author Cameron
 */
@Data
@AllArgsConstructor
public class ChallengeWithTimestamp {
    private String challenge;
    private long timestamp;
    private long validityPeriodInSeconds;

    public ChallengeWithTimestamp(String challenge, long validityPeriodInSeconds) {
        this.challenge = challenge;
        this.timestamp = System.currentTimeMillis();
        this.validityPeriodInSeconds = validityPeriodInSeconds;

    }

    /**
     * Check if the challenge has expired
     * @return true if the challenge has expired
     */
    public boolean isExpired(long validityPeriodInSeconds) {
        return System.currentTimeMillis() - timestamp > validityPeriodInSeconds * 1000L;
    }
}
