package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for managing challenges in the cache
 * Challenges are used to authenticate users
 * @author Cameron
 */
@NoArgsConstructor
@Service
public class ChallengeService {

    /**
     * Save the challenge for the user in the cache
     * Cache key: key, value: challenge
     * @param key the user to save the challenge for
     * @param challenge the challenge to save
     * @return the saved challenge
     */
    @CachePut(value = "challenges", key = "#key")
    public ChallengeWithTimestamp saveChallengeForUser(String key, String challenge) {
        return new ChallengeWithTimestamp(challenge);
    }


    /**
     * Retrieve the challenge for the user from the cache
     * @param key the key of the user to retrieve the challenge for
     * @return the challenge for the user, or null if not found
     */
    @Cacheable(value = "challenges", key = "#key")
    public ChallengeWithTimestamp getChallengeForUser(String key) {
        return null;
    }

    /**
     * Evict the challenge cache for a specific user.
     * This method will be invoked when the challenge is validated.
     * @param key the key of the user whose challenge cache should be evicted
     */
    @CacheEvict(value = "challenges", key = "#key")
    public void evictChallengeCache(String key) {}
}
