package grapes.microservices.authservice.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Base64;

@Configuration
public class AESConfig {

    @Value("${auth.service.aes.secret.key}")
    private String aesKeyBase64;

    @Getter
    private static byte[] aesKey;

    @PostConstruct
    public void init() {
        if (aesKeyBase64 != null) {
            aesKey = Base64.getDecoder().decode(aesKeyBase64);
            System.out.println("AES Key loaded: " + Base64.getEncoder().encodeToString(aesKey));
        } else {
            System.err.println("AES key is not configured properly.");
        }
    }

    @Bean
    public static byte[] getKey() {
        return aesKey;
    }
}
