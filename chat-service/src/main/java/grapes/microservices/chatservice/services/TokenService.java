package grapes.microservices.chatservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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

    @Value("${authentication.jwt.key}")
    private String secretKey;
    @Value("${authentication.jwt.expiration.time}")
    private long EXPIRATION_TIME;

    private SecretKey SECRET_KEY;

    /**
     * Initializes the service by loading the secret key
     * If the secret key is not configured properly, a new one is generated
     * If a new key is generated, it is printed in the console
     * If a new key is generated, the application will stop
     */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        if (secretKey == null) {
            throw new RuntimeException("JWT secret key is not configured properly.");
        } else {
            try {
                if (secretKey.length() < 32) {
                    throw new RuntimeException("JWT secret key is too short.");
                }
                SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
            } catch (Exception e) {
                // Generate a new secret key
                KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
                keyGenerator.init(256);
                SECRET_KEY = keyGenerator.generateKey();

                // Display the new secret key and stop the application
                secretKey = Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
                System.out.println("Generated new JWT secret key: " + secretKey);
                System.out.println("Please put this in your environment variables.");
                throw new RuntimeException("Error loading JWT secret key: " + e.getMessage());
            }
        }
    }

    /**
     * Generates a token for the given email
     * The token is valid for 24 hours
     *
     * @param idStr the identifier to be used in the token
     * @return the generated token
     */
    public String generateToken(String idStr, String name) {
        return Jwts.builder()
                .setSubject(idStr)
                .claim("name", name)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 60 * 1000))
                .signWith(SECRET_KEY)
                .compact();
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
     * Extracts the username from the given token
     *
     * @param token the token to extract the user ID from
     * @return the username
     */
    public String extractUserName(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSECRET_KEY())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("name", String.class);
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
            return claims.getExpiration().after(new Date());
        } catch (SignatureException e) {
            return false;
        }
    }
}