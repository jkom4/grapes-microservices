package grapes.microservices.apigateway.security;

import grapes.microservices.apigateway.service.AuthSessionService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
            "/actuator/*"
    );

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String USER_NAME_HEADER = "X-User-Name";

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final AuthSessionService authSessionService;

    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil, ReactiveRedisTemplate<String, String> redisTemplate, AuthSessionService authSessionService) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.authSessionService = authSessionService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        if (isWhitelisted(path)) {
            logger.warn("whitelisted path: {}", path);
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null) {
            return unauthorizedResponse(exchange, "Authorization header missing or invalid");
        }

        if (!jwtUtil.validateToken(token)) {
            return unauthorizedResponse(exchange, "Invalid JWT token");
        }

        return redisTemplate.opsForValue().get("invalidated:" + token)
                .flatMap(invalidated -> {
                    if (invalidated != null) {
                        return unauthorizedResponse(exchange, "Token revoked");
                    }

                    Claims claims = jwtUtil.extractAllClaims(token);
                    String userId = claims.getSubject();

                    return redisTemplate.opsForHash().hasKey("session:" + userId, "refresh")
                            .flatMap(hasRefresh -> {
                                if (Boolean.FALSE.equals(hasRefresh)) {
                                    // 📡 Appel Auth Service pour récupérer refresh_token
                                    return authSessionService.initSession(token) // 👉 ta méthode existante
                                            .flatMap(tokens -> {
                                                Map<String, String> sessionData = Map.of(
                                                        "access", tokens.getAccessToken(),
                                                        "refresh", tokens.getRefreshToken()
                                                );

                                                return redisTemplate.opsForHash()
                                                        .putAll("session:" + userId, sessionData)
                                                        .then(redisTemplate.expire("session:" + userId, Duration.ofHours(1)))
                                                        .then(injectClaimsAndContinue(token, exchange, chain));
                                            });
                                } else {
                                    return injectClaimsAndContinue(token, exchange, chain);
                                }
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String userId = claims.getSubject();

                    return redisTemplate.opsForHash().hasKey("session:" + userId, "refresh")
                            .flatMap(hasRefresh -> {
                                if (Boolean.FALSE.equals(hasRefresh)) {
                                    return authSessionService.initSession(token) // 👉 ici aussi
                                            .flatMap(tokens -> {
                                                Map<String, String> sessionData = Map.of(
                                                        "access", tokens.getAccessToken(),
                                                        "refresh", tokens.getRefreshToken()
                                                );

                                                return redisTemplate.opsForHash()
                                                        .putAll("session:" + userId, sessionData)
                                                        .then(redisTemplate.expire("session:" + userId, Duration.ofHours(1)))
                                                        .then(injectClaimsAndContinue(token, exchange, chain));
                                            });
                                } else {
                                    return injectClaimsAndContinue(token, exchange, chain);
                                }
                            });
                }));
    }

    private Mono<Void> injectClaimsAndContinue(String token, ServerWebExchange exchange, GatewayFilterChain chain) {
        Claims claims = jwtUtil.extractAllClaims(token);
        String userId = claims.getSubject();
        String roles = claims.get("roles", String.class);
        String name = claims.get("name", String.class); // si "name" est dans les claims

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header(USER_ID_HEADER, userId)
                .header(ROLES_HEADER, roles)
                .header(USER_NAME_HEADER, name != null ? name : "")
                .build();

        logger.info("Authenticated request - User: {}, Name: {}, Roles: {}, Path: {}",
                userId, name, roles, exchange.getRequest().getPath());

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
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