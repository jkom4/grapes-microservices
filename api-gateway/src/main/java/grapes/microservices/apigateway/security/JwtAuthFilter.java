package grapes.microservices.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final List<String> WHITELISTED_ENDPOINTS = List.of(
            "/api/auth/**",
            "/api/users/**",
            "/api/clm/**",
            "/api/cll/**",
            "/api/chat/**",
            "/api/payment/**",
            "/actuator/*"
    );
    @Value("${auth.service.url}")
    private String authServiceUrl;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String USER_NAME_HEADER = "X-User-Name";

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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

        // Si access token est expiré
        if (!jwtUtil.validateToken(token)) {
            Claims expiredClaims = jwtUtil.extractAllClaims(token);
            String userId = expiredClaims.getSubject();

            // Vérifier s’il y a une session en cache
            return redisTemplate.opsForValue().get("session:" + userId)
                    .flatMap(refreshToken -> {
                        if (refreshToken == null) {
                            logger.warn("No session found for user {}", userId);
                            return unauthorizedResponse(exchange, "Session expired");
                        }

                        // Appeler le serveur d'auth pour obtenir un nouveau accessToken
                        return fetchNewAccessTokenFromAuthService(refreshToken)
                                .flatMap(newToken -> {
                                    // Stocker dans la session ou remplacer
                                    return forwardRequestWithToken(newToken, exchange, chain);
                                });
                    });
        }

        // Si accessToken est valide
        Claims claims = jwtUtil.extractAllClaims(token);
        String userId = claims.getSubject();
        String roles = claims.get("roles", String.class);

        // Si la session n'existe pas encore, on la crée ici
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
                        forwardRequestWithClaims(exchange, chain, request, userId, roles)
                );
    }
    private Mono<Void> forwardRequestWithClaims(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request, String userId, String roles) {
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(USER_ID_HEADER, userId)
                .header(ROLES_HEADER, roles)
                .header(USER_NAME_HEADER, roles) // si tu as username séparé, change ça
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<String> fetchRefreshTokenFromAuthService(String accessToken) {
        WebClient client = WebClient.create(authServiceUrl);
        return client.post()
                .uri("/api/auth/get-refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<String> fetchNewAccessTokenFromAuthService(String refreshToken) {
        WebClient client = WebClient.create(authServiceUrl);
        return client.post()
                .uri("/api/auth/refresh")
                .header("X-Refresh-Token", refreshToken)
                .bodyValue(Collections.singletonMap("refreshToken", refreshToken))
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<Void> forwardRequestWithToken(String newToken, ServerWebExchange exchange, GatewayFilterChain chain) {
        Claims claims = jwtUtil.extractAllClaims(newToken);
        String userId = claims.getSubject();
        String roles = claims.get("roles", String.class);

        return forwardRequestWithClaims(exchange, chain, exchange.getRequest(), userId, roles);
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