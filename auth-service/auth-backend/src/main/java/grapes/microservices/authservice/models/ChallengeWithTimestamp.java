package grapes.microservices.authservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

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

    private int VALIDITY_PERIOD = 180; //in seconds

    public ChallengeWithTimestamp(String challenge) {
        this.challenge = challenge;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Check if the challenge has expired
     * @return true if the challenge has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > VALIDITY_PERIOD * 1000L;
    }
}
