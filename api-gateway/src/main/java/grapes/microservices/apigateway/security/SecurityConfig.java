package grapes.microservices.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] AUTH_PATHS = { "/api/auth/verify-challenge","/api/auth/login","/api/auth/register","/api/auth/session","/api/users/**"};
    private static final String[] SALES_PATHS = { "/api/clm/articles","/api/cll/**"};

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // Désactive le login form
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Désactive Basic Auth
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(AUTH_PATHS).permitAll()
                        .pathMatchers(SALES_PATHS).permitAll()
                        .anyExchange().authenticated()
                ).build();
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
//                        .build());
    }
}
