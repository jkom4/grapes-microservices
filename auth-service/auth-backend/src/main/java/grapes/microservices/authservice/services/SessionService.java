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

    public void deleteSession(String userId) {
        redisTemplate.delete("session:" + userId);
    }
}
