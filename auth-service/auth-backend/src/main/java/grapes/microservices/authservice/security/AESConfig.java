package grapes.microservices.authservice.security;

import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.KeyGenerator;
import java.util.Base64;


/**
 * AESConfig is a configuration class responsible for managing AES encryption keys.
 * It initializes an AES key from a Base64-encoded string and provides a method
 * for generating a new AES-256 key.
 * @author Cameron
 **/
@Configuration
public class AESConfig {

    @Value("${auth.service.aes.secret.key}")
    private String aesKeyBase64;

    @Getter
    private static byte[] aesKey;

    private static final String AES_ALGORITHM = "AES";

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @PostConstruct
    public void init() {
        if (aesKeyBase64 != null) {
            aesKey = Base64.getDecoder().decode(aesKeyBase64);
            logger.info("AES Key loaded.");
        } else {
            logger.error("AES key is not configured properly.");
            System.err.println("AES key is not configured properly.");
        }
    }

    /**
     * Generates a new AES-256 key.
     *
     * @return A byte array containing the AES-256 key.
     * @throws Exception If key generation fails.
     */
    public static byte[] generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(256); // 256-bit key
        return keyGenerator.generateKey().getEncoded();
    }

    @Bean
    public static byte[] getKey() {
        return aesKey;
    }
}
