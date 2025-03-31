package grapes.microservices.authservice.services;

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
     * Cache key: email, value: challenge
     * @param email the user to save the challenge for
     * @param challenge the challenge to save
     * @return the saved challenge
     */
    @CachePut(value = "challenges", key = "#email")
    public String saveChallengeForUser(String email, String challenge) {
        return challenge;
    }


    /**
     * Retrieve the challenge for the user from the cache
     * @param email the email of the user to retrieve the challenge for
     * @return the challenge for the user, or null if not found
     */
    @Cacheable(value = "challenges", key = "#email")
    public String getChallengeForUser(String email) {
        return null;
    }

    /**
     * Evict the challenge cache for a specific user.
     * This method will be invoked when the challenge is validated.
     * @param email the email of the user whose challenge cache should be evicted
     */
    @CacheEvict(value = "challenges", key = "#email")
    public void evictChallengeCache(String email) {}
}
