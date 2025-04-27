package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.Role;
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
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

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

    @Value("${auth.service.access.token.expiration.time.minutes}")
    private long ACCESS_EXPIRATION_TIME;

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
     *
     * @param idStr the identifier to be used in the token
     * @return the generated token
     */
    public String generateToken(String idStr, String name, Role role) {
        return Jwts.builder()
                .setSubject(idStr)
                .claim("name", name)
                .claim("role", role.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME * 60 * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Generates a secure opaque refresh token for the specified user.
     * Unlike access tokens, which are stateless JWTs containing user claims and signed cryptographically,
     * refresh tokens are implemented here as opaque random UUID strings. This is a common approach in enterprise applications
     * where refresh tokens are:
     * <ul>
     *     <li>Stored securely on the server (e.g., Redis or database)</li>
     *     <li>Not parseable by the client (no embedded claims)</li>
     *     <li>Easily revocable by deleting the entry from the server</li>
     * </ul>
     *
     * @return the newly generated refresh token as a random UUID string
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extracts the user ID from the given token
     *
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
     * Extracts the user's role from the given token
     *
     * @param token the token to extract the user's role from
     * @return the user's role
     */
    public String extractUserRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSECRET_KEY())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the token is valid
     * The token is valid if it is not expired and the signature is valid
     *
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