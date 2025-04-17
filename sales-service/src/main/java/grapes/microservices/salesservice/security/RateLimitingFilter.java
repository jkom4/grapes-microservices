package grapes.microservices.salesservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter
 *
 * This filter implements request rate limiting to protect the service against excessive traffic.
 * It tracks request frequency from clients based on their IP address or other identifiers,
 * and temporarily blocks requests that exceed configured thresholds.
 *
 * The filter helps mitigate:
 * - Denial of Service (DoS) attacks
 * - Distributed Denial of Service (DDoS) attacks
 * - Unintentional service overloading from legitimate clients
 *
 * Configuration parameters:
 * - maxRequestsPerMinute: Maximum number of requests allowed per client within a minute
 * - blockDurationSeconds: Duration in seconds a client is blocked after exceeding the limit
 */

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60; // Limit to 60 requests per minute
    private static final long TIME_WINDOW_MILLIS = 60_000; // 1 minute window in milliseconds

    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();  // or use a token / user ID if authenticated

        UserRequestInfo info = requestCounts.computeIfAbsent(clientIp, k -> new UserRequestInfo());

        synchronized (info) {
            long currentTime = Instant.now().toEpochMilli();
            if (currentTime - info.startTime > TIME_WINDOW_MILLIS) {
                // Reset the window if the time has elapsed
                info.startTime = currentTime;
                info.requestCount = 0;
            }

            info.requestCount++;

            if (info.requestCount > MAX_REQUESTS_PER_MINUTE) {
                // Block the request if the user exceeded the rate limit
                response.setStatus(429);
                response.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        // Continue the request chain
        filterChain.doFilter(request, response);
    }

    private static class UserRequestInfo {
        long startTime = Instant.now().toEpochMilli(); // Start time of the current window
        int requestCount = 0;                          // Number of requests made in the current window
    }
}
