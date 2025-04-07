package grapes.microservices.authservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for managing user sessions
 * Sessions are stored in Redis
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate redisTemplate;

    private static final long EXPIRATION = 20; // Expiration in minutes
    private static final long EXPIRATION_REFRESH = 7; // Expiration in days

    /**
     * Saves a session in Redis
     * The session is stored with the key "session:userId"
     * The session token is stored with the key "refresh:userId"
     * Automatically expires after 20 minutes
     * @param userId the user id
     * @param token the session token
     */
    public void saveSession(String userId, String token) {
        redisTemplate.opsForValue().set("session:" + userId, token, EXPIRATION, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("refresh:" + userId, token, EXPIRATION_REFRESH, TimeUnit.DAYS);
    }

    public String getSession(String userId) {
        return redisTemplate.opsForValue().get("session:" + userId);
    }

    public String getRefresh(String userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    /**
     * Resets the session for the given user ID
     * Deletes the session and refresh token from Redis
     * @param userId the user id
     */
    public void resetSession(String userId) {
        String sessionKey = "session:" + userId;
        String refreshKey = "refresh:" + userId;

        if (redisTemplate.hasKey(sessionKey)) {
            redisTemplate.delete(sessionKey);
        }
        if (redisTemplate.hasKey(refreshKey)) {
            redisTemplate.delete(refreshKey);
        }
    }

    /**
     * Deletes the session for the given user ID
     * @param userId the user id
     */
    public void deleteSession(String userId) {
        String sessionKey = "session:" + userId;
        String refreshKey = "refresh:" + userId;

        if (!redisTemplate.hasKey(sessionKey)) {
            throw new RuntimeException("Session does not exist");
        }

        redisTemplate.delete(sessionKey);
        redisTemplate.delete(refreshKey);
    }

    /**
     * Checks if a session is currently open for the given user ID
     * @param userId the user id
     * @return true if a session exists, false otherwise
     */
    public boolean isSessionOpen(String userId) {
        return redisTemplate.hasKey("session:" + userId);
    }
}
