package grapes.microservices.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A servlet filter that limits the number of requests a client can make per minute based on their IP address.
 * <p>
 * This filter allows a maximum of {@link #MAX_REQUESTS_PER_MINUTE} requests per {@link #DURATION} seconds.
 * If the limit is exceeded, the filter returns HTTP 429 (Too Many Requests) and does not proceed further.
 * </p>
 *
 * @author Cameron
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${auth.service.max.requests.per.minute}")
    private int MAX_REQUESTS_PER_MINUTE;
    private static final long DURATION = 60;

    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        UserRequestInfo info = requestCounts.computeIfAbsent(clientIp, k -> new UserRequestInfo());

        synchronized (info) {
            long currentTime = Instant.now().toEpochMilli();
            if (currentTime - info.startTime > DURATION * 1000) {
                // Reset the window if the time has elapsed
                info.startTime = currentTime;
                info.requestCount = 0;
            }

            info.requestCount++;

            if (info.requestCount > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        // Continue the request chain
        filterChain.doFilter(request, response);
    }

    private static class UserRequestInfo {
        long startTime = Instant.now().toEpochMilli();
        int requestCount = 0;
    }
}
