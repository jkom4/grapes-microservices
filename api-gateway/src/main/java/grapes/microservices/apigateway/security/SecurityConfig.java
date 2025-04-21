package grapes.microservices.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] AUTH_PATHS = { "/api/auth/**","/api/users/**"};
    private static final String[] SALES_PATHS = {
            "/api/cll/**",
            "/api/clm/**",
    };
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

    @Bean
    public CorsWebFilter corsFilter() {
        return new CorsWebFilter(exchange -> {
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedOrigin("http://localhost:3000");
            config.addAllowedOrigin("http://localhost:3001");
            config.addAllowedOrigin("http://localhost:3000");
            config.addAllowedOrigin("http://localhost:81");
            config.addAllowedOrigin("http://localhost:80");
            config.addAllowedOrigin("http://localhost");
            config.addAllowedOrigin("http://localhost:82");
            config.addAllowedMethod("*");
            config.addAllowedHeader("*");
            return config;
        });
    }
}
