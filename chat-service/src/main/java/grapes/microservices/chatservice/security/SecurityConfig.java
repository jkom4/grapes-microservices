package grapes.microservices.chatservice.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Allow unauthenticated access to Swagger resources only
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**"
    };
    //TO REMOVE FOR PROD
    private static final String[] CHAT_PATHS = {
            "/chat/**",
            "/actuator/**"
    };

    // OpenAPI configuration for Swagger UI with Bearer authentication
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SWAGGER_PATHS).permitAll()   // Allow Swagger paths without authentication
                        .requestMatchers(CHAT_PATHS).permitAll()   // TO REMOVE FOR PRODUCTION
                        .anyRequest().authenticated());              // All other requests require authentication
        return http.build();
    }
}
