package grapes.microservices.chatservice.services;

import grapes.microservices.chatservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public UserDto validateToken(String token) {
        log.info("Validating token with Auth-Service...");

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/auth/" + token)
                .retrieve()
                .bodyToMono(UserDto.class)
                .onErrorResume(e -> {
                    log.error("Token validation failed: {}", e.getMessage());
                    return Mono.empty();
                })
                .block(); //no-réactif
    }
}
