package grapes.microservices.apigateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final List<String> WHITELISTED_ENDPOINTS = List.of(
            "/api/auth/**",
            "/api/users/**",
            "/api/clm/**",
            "/api/cll/**",
            "/api/payment/**",
            "/actuator/*"
    );
    @Value("${auth.service.url}")
    private String authServiceUrl;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String USER_NAME_HEADER = "X-User-Name";

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        if (isWhitelisted(path)) {
            logger.info("Whitelisted path: {}", path);
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null) {
            return unauthorizedResponse(exchange, "Authorization header missing or invalid");
        }

        if (!jwtUtil.validateToken(token)) {
            Claims expiredClaims;
            try {
                expiredClaims = jwtUtil.extractAllClaims(token);
            } catch (ExpiredJwtException e) {
                expiredClaims = e.getClaims();
            }

            String userId = expiredClaims.getSubject();

            // verify session
            return redisTemplate.opsForValue().get("session:" + userId)
                    .flatMap(jsonRefreshFromRedis  -> {
                        if (jsonRefreshFromRedis == null) {
                            logger.warn("No session found for user {}", userId);
                            return unauthorizedResponse(exchange, "Session expired");
                        }

                        Map<String, String> map = null;
                        try {
                            map = objectMapper.readValue(jsonRefreshFromRedis, new TypeReference<Map<String, String>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        String refreshToken = map.get("refreshToken");

                        return fetchNewAccessTokenFromAuthService(refreshToken)
                                .flatMap(newToken -> {
                                    // save in session
                                    return forwardRequestWithToken(newToken, exchange, chain);
                                });
                    });
        }

        // If token is valid
        Claims claims = jwtUtil.extractAllClaims(token);
        String userId = claims.getSubject();
        String roles = claims.get("role", String.class);
        String name = claims.get("name", String.class);

        // if no session create
        return redisTemplate.opsForValue().get("session:" + userId)
                .switchIfEmpty(
                        fetchRefreshTokenFromAuthService(token)
                                .flatMap(refreshToken -> {
                                    logger.info("Saving new session for user {}", userId);
                                    return redisTemplate.opsForValue()
                                            .set("session:" + userId, refreshToken)
                                            .then(Mono.just(refreshToken));
                                })
                )
                .then(
                        forwardRequestWithClaims(exchange, chain, request, userId, roles, name)
                );
    }
    private Mono<Void> forwardRequestWithClaims(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request, String userId, String roles, String name) {
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(USER_ID_HEADER, userId)
                .header(ROLES_HEADER, roles)
                .header(USER_NAME_HEADER, name)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<String> fetchRefreshTokenFromAuthService(String accessToken) {
        WebClient client = WebClient.create(authServiceUrl);
        return client.post()
                .uri("/auth/get-refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<String> fetchNewAccessTokenFromAuthService(String refreshToken) {
        WebClient client = WebClient.create(authServiceUrl);
        return client.post()
                .uri("/auth/refresh")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Collections.singletonMap("refreshToken", refreshToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .map(response -> response.get("accessToken"));
    }

    private Mono<Void> forwardRequestWithToken(String newToken, ServerWebExchange exchange, GatewayFilterChain chain) {
        Claims claims = jwtUtil.extractAllClaims(newToken);
        String userId = claims.getSubject();
        String roles = claims.get("role", String.class);
        String name = claims.get("name", String.class);

        return forwardRequestWithClaims(exchange, chain, exchange.getRequest(), userId, roles, name);
    }



    @Override
    public int getOrder() {
        return -100; // High priority
    }

    private boolean isWhitelisted(String path) {
        return WHITELISTED_ENDPOINTS.stream()
                .anyMatch(whitelist -> pathMatcher.match(whitelist, path));
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        logger.warn("Authentication failed: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"code\": 401}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}