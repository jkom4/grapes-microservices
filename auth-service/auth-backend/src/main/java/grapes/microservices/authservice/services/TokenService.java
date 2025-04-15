package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.utils.AuthLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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

    @Value("${auth.service.jwt.expiration.time}")
    private long expirationTime;

    @Autowired
    private SessionService sessionService;

    private SecretKey SECRET_KEY;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    /**
     * Initializes the service by loading the secret key
     * If the secret key is not configured properly, a new one is generated
     * If a new key is generated, it is printed in the console
     * If a new key is generated, the application will stop
     */
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

                // Generate a new secret key
                KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
                keyGenerator.init(256);
                SECRET_KEY = keyGenerator.generateKey();

                // Display the new secret key and stop the application
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
     * @param idStr the identifier to be used in the token
     * @return the generated token
     */
    public String generateToken(String idStr) {
        return Jwts.builder()
                .setSubject(idStr)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Get the refresh token for the given user ID from the session service
     * @param userId the user ID
     * @return the refresh token
     */
    public String getRefreshToken(String userId) {
        String refreshToken = sessionService.getRefresh(userId);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found.");
        }
        return refreshToken;
    }

    /**
     * Extracts the user ID from the given token
     * @param token the token to extract the user ID from
     * @return the user ID
     */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSECRET_KEY())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates the given token
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Checks if the token is valid
     * The token is valid if it is not expired and the signature is valid
     * @param token the token to check
     * @return true if the token is valid, false otherwise
     */
    public boolean isValidToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build();

            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new java.util.Date());
        } catch (SignatureException e) {
            return false;
        }
    }
}
