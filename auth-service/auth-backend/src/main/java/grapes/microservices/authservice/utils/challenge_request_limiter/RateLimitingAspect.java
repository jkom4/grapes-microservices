package grapes.microservices.authservice.utils.challenge_request_limiter;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.utils.exceptions.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Aspect that enforces rate limiting on methods annotated with {@link OneCallPerMinutePerUser}.
 * <p>
 * This aspect uses a {@link RedisRateLimiter} to ensure that a user can only request a challenge
 * once per defined time interval. It extracts the {@link User} from the method arguments and
 * checks the user's email to apply the rate-limiting rule.
 * </p>
 *
 * @author Cameron
 */
@Aspect
@Component
public class RateLimitingAspect {

    private final RedisRateLimiter redisRateLimiter;

    /**
     * Interval (in seconds) that must pass before a user can make another challenge request.
     * This value is injected from the application properties using the key:
     * <code>auth.service.challenge.request.interval</code>.
     */
    @Value("${auth.service.challenge.request.interval}")
    private long CHALLENGE_REQUEST_INTERVAL_IN_SECONDS;

    @Autowired
    public RateLimitingAspect(RedisRateLimiter redisRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
    }

    /**
     * Intercepts method calls annotated with {@link OneCallPerMinutePerUser} and enforces
     * rate limiting based on the user's email. If the user has exceeded the allowed rate,
     * a {@link RateLimitExceededException} is thrown.
     *
     * @param joinPoint the join point representing the intercepted method call
     * @return the result of the method call if the rate limit is not exceeded
     * @throws Throwable if the intercepted method throws an exception or the rate limit is exceeded
     */
    @Around("@annotation(OneCallPerMinutePerUser)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof User user) {
                String email = user.getEmail();
                if (!redisRateLimiter.isAllowed(email)) {
                    throw new RateLimitExceededException("You can only request a challenge once per " + CHALLENGE_REQUEST_INTERVAL_IN_SECONDS + " seconds.");
                }
                break;
            }
        }

        return joinPoint.proceed();
    }
}
