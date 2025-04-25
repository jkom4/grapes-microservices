package grapes.microservices.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import grapes.microservices.apigateway.models.SessionData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthSessionService {

    private final RedisTemplate<String, SessionData> redisTemplate;
    private final WebClient webClient;

    public AuthSessionService(RedisTemplate<String, SessionData> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.webClient = WebClient.create();
    }

    public void saveSession(String userId, SessionData data) {
        redisTemplate.opsForValue().set("SESSION:" + userId, data);
    }

    public SessionData getSession(String userId) {
        return redisTemplate.opsForValue().get("SESSION:" + userId);
    }

    public Mono<SessionData> initSession(String accessToken) {
        return webClient.post()
                .uri("http://localhost:8080/auth/session/init") // 👈 adapte cette URI à ton infra
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    String refreshToken = json.get("refresh_token").asText();
                    String newAccessToken = json.has("access_token") ? json.get("access_token").asText() : accessToken;
                    long expiresAt = System.currentTimeMillis() + json.get("expires_in").asLong() * 1000;
                    return new SessionData(newAccessToken, refreshToken, expiresAt);
                });
    }


    public Mono<SessionData> refreshSession(String userId, SessionData session) {
        return webClient.post()
                .uri("https://authserver.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", session.getRefreshToken())
                        .with("client_id", "your-client-id")
                        .with("client_secret", "your-client-secret"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    String newAccessToken = json.get("access_token").asText();
                    long expiresAt = System.currentTimeMillis() + json.get("expires_in").asLong() * 1000;
                    String refreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : session.getRefreshToken();
                    SessionData updated = new SessionData(newAccessToken, refreshToken, expiresAt);
                    saveSession(userId, updated);
                    return updated;
                });
    }
}