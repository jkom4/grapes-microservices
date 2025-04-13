package grapes.microservices.salesservice.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**"
    };

    /**
     * Swagger configuration to permit authentification with JWT
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * Rule of security configuration
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers("/articles/**").permitAll()// public Swagger
                       // .requestMatchers("/articles/**").authenticated()  // Articles :  authentification required
                        .anyRequest().permitAll()                         // others accessibles
                );

        // ⚠️ To complete with JWT configuration if he works (filter, etc.)

        return http.build();
    }

    /**
     * Registration of the Rate Limiting Filter
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(rateLimitingFilter);
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        registrationBean.setOrder(1); // High priority
        return registrationBean;
    }
}
