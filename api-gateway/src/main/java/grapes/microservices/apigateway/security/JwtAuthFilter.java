package grapes.microservices.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final List<String> WHITELISTED_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/users/**",
            "api/auth/register",
            "/api/auth/verify-challenge",
            "/actuator/health"
    );

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLES_HEADER = "X-User-Roles";

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Bypass pour les endpoints publics
        if (isWhitelisted(path)) {
            logger.warn("whitelisted path: {}", path);
            return chain.filter(exchange);
        }

        // Extraction du token
        String token = extractToken(request);
        if (token == null) {
            return unauthorizedResponse(exchange, "Authorization header missing or invalid");
        }

        // Validation synchrone si nécessaire (sinon version réactive ci-dessous)
        if (!jwtUtil.validateToken(token)) {
            return unauthorizedResponse(exchange, "Invalid JWT token");
        }

        // Vérification dans Redis (version réactive)
        return redisTemplate.opsForValue().get("invalidated:" + token)
                .flatMap(invalidated -> {
                    if (invalidated != null) {
                        return unauthorizedResponse(exchange, "Token revoked");
                    }

                    Claims claims = jwtUtil.extractAllClaims(token);
                    String userId = claims.getSubject();
                    String roles = claims.get("roles", String.class);

                    // Ajout des headers pour les microservices downstream
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header(USER_ID_HEADER, userId)
                            .header(ROLES_HEADER, roles)
                            .build();

                    logger.info("Authenticated request - User: {}, Path: {}", userId, path);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Cas où le token n'est pas dans Redis (non révoqué)
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String userId = claims.getSubject();
                    String roles = claims.get("roles", String.class);

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header(USER_ID_HEADER, userId)
                            .header(ROLES_HEADER, roles)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }));
    }

    @Override
    public int getOrder() {
        return -100; // Hautement prioritaire
    }

    private boolean isWhitelisted(String path) {
        return WHITELISTED_ENDPOINTS.stream().anyMatch(path::startsWith);
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