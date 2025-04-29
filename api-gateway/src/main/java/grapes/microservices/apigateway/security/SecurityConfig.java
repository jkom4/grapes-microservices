package grapes.microservices.apigateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] AUTH_PATHS = { "/api/auth/**","/api/users/**"};
    private static final String[] SALES_PATHS = {"/api/cll/**","/api/clm/**"};
    private static final String[] CHAT_PATHS = {"/api/chat/**"};
    private static final String[] PAYMENT_PATHS = {"/api/payment/**"};
    private static final String[] MONITORING_PATHS = {"/actuator/*"};
    @Value("${auth.frontend.url}")
    private String authfrontendurl;
    @Value("${payment.frontend.url}")
    private String paymentfrontendurl;
    @Value("${frontend.url}")
    private String frontendurl;
    @Value("${grapes.frontend.url}")
    private String grapesfrontendurl;
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // Désactive le login form
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Désactive Basic Auth
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(AUTH_PATHS).permitAll()
                        .pathMatchers(SALES_PATHS).permitAll()
                        .pathMatchers(CHAT_PATHS).permitAll()
                        .pathMatchers(PAYMENT_PATHS).permitAll()
                        .pathMatchers(MONITORING_PATHS).permitAll()
                        .anyExchange().authenticated()
                ).build();
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
//                        .build());
    }

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
                frontendurl,
                grapesfrontendurl,
                authfrontendurl,
                paymentfrontendurl
        ));


        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization", "x-requested-with"));
        config.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
