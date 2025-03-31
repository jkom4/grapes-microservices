package grapes.microservices.authservice.services;

import grapes.microservices.authservice.utils.AuthLogger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

/**
 * Service for generating and validating tokens
 * @author cameron
 */
@Service
@RequiredArgsConstructor
@Getter
public class TokenService {

    @Value("${auth.service.jwt.secret.key}")
    private String secretKey;

    private SecretKey SECRET_KEY;

    private static final Logger logger = AuthLogger.getLogger();

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        if (secretKey == null) {
            logger.error("JWT secret key is not configured properly.");
            throw new RuntimeException("JWT secret key is not configured properly.");
        } else {
            try {
                if (secretKey.length() < 32) {
                    logger.error("JWT secret key is too short, must be at least 256 bits (32 bytes).");
                    throw new RuntimeException("JWT secret key is too short.");
                }
                SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
            } catch (Exception e) {
                logger.error("Error loading JWT secret key: " + e.getMessage());
                logger.info("Generating new JWT secret key...");

                KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
                keyGenerator.init(256);
                SECRET_KEY = keyGenerator.generateKey();

                secretKey = Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
                System.out.println("Generated new JWT secret key: " + secretKey);
                System.out.println("Please put this in your environment variables.");
                logger.info("New JWT secret key generated (please see in console and put it in your environment variables).");
                throw new RuntimeException("Error loading JWT secret key: " + e.getMessage());
            }
            logger.info("JWT secret key loaded.");
        }
    }

    /**
     * Generates a token for the given email
     * The token is valid for 24 hours
     * @param email the email to generate the token for
     * @return the generated token
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SECRET_KEY)
                .compact();
    }
}
