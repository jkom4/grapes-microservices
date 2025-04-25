package grapes.microservices.authservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${auth.service.refresh.token.expiration.time.days}")
    private long REFRESH_EXPIRATION_TIME; // Expiration in days

    /**
     * Saves a session in Redis and deletes any existing session
     * The session is stored with the key "session:userId"
     * The session token is stored with the key "refresh:userId"
     * @param userId the user id
     * @param refreshToken the refresh token
     */
    public void saveSession(String userId, String refreshToken) {
        String refreshKey = "refresh:" + userId;

        redisTemplate.delete(refreshKey);

        redisTemplate.opsForValue().set(refreshKey, refreshToken, REFRESH_EXPIRATION_TIME, TimeUnit.DAYS);
    }

    /**
     * Finds the userId associated with the given refresh token
     * @param refreshToken the refresh token
     * @return the userId if found, otherwise null
     */
    public String getUserIdByRefresh(String refreshToken) {
        for (String key : redisTemplate.keys("refresh:*")) {
            String storedRefreshToken = redisTemplate.opsForValue().get(key);
            if (refreshToken.equals(storedRefreshToken)) {
                return key.replace("refresh:", "");
            }
        }
        throw new IllegalArgumentException("Refresh token not found");
    }

    /**
     * Finds the refresh token associated with the given userId
     * @param userId the user id
     * @return the refresh token if found, otherwise null
     */
    public String getRefreshTokenByUserId(String userId) {
        String refreshKey = "refresh:" + userId;
        return redisTemplate.opsForValue().get(refreshKey);
    }

    /**
     * Deletes the session for the given user ID
     * @param userId the user id
     */
    public void deleteSession(String userId) {
        String refreshKey = "refresh:" + userId;

        redisTemplate.delete(refreshKey);
    }
}
