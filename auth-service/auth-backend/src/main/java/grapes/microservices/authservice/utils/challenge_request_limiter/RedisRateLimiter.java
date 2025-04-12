package grapes.microservices.authservice.utils.challenge_request_limiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Limits the number of requests a user can make
 */
@Service
public class RedisRateLimiter {

    @Value("${auth.service.challenge.request.interval}")
    private long CHALLENGE_REQUEST_INTERVAL_IN_SECONDS;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks if the rate limit is exceeded for a given key.
     * @param key the key to check
     * @return true if the rate limit is not exceeded, false otherwise
     */
    public boolean isAllowed(String key) {
            String redisKey = "rate-limit:" + key;

        Boolean alreadyExists = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(alreadyExists)) {
            return false;
        }

        redisTemplate.opsForValue().set(redisKey, "1", CHALLENGE_REQUEST_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        return true;
    }
}
