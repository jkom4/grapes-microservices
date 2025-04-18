package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AuthToken model
 * Tests constructors, validity checks, getters, setters, and object methods
 */
public class AuthTokenTest {

    private AuthToken authToken;

    @Mock
    private Client mockClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authToken = new AuthToken("test-token-1234", mockClient);
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty AuthToken with all fields null/default
     */
    @Test
    public void testNoArgsConstructor() {
        AuthToken token = new AuthToken();
        assertNotNull(token);
        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getCreatedAt());
        assertNull(token.getExpiresAt());
        assertFalse(token.isUsed());
        assertNull(token.getClient());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates an AuthToken with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(1);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2);

        AuthToken token = new AuthToken(
                1L,
                "all-args-token",
                createdAt,
                expiresAt,
                true,
                mockClient
        );

        assertEquals(1L, token.getId());
        assertEquals("all-args-token", token.getToken());
        assertEquals(createdAt, token.getCreatedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertTrue(token.isUsed());
        assertEquals(mockClient, token.getClient());
    }

    /**
     * Tests the parameterized constructor (token, client)
     * Expected: Sets token and client, initializes timestamps with default 3-minute expiration
     */
    @Test
    public void testParameterizedConstructor() {
        // Verify token and client are set correctly
        assertEquals("test-token-1234", authToken.getToken());
        assertEquals(mockClient, authToken.getClient());

        // Verify default values
        assertFalse(authToken.isUsed());
        assertNull(authToken.getId());

        // Verify timestamps initialization
        assertNotNull(authToken.getCreatedAt());
        assertNotNull(authToken.getExpiresAt());

        // Verify expiration is 3 minutes after creation
        LocalDateTime createdAt = authToken.getCreatedAt();
        LocalDateTime expiresAt = authToken.getExpiresAt();

        long minutesDifference = ChronoUnit.MINUTES.between(createdAt, expiresAt);
        assertEquals(3, minutesDifference);
    }

    /**
     * Tests isValid method when token is not used and not expired
     * Expected: Returns true for valid tokens
     */
    @Test
    public void testIsValid_WhenNotUsedAndNotExpired() {
        // Set expiration to 10 minutes in the future
        LocalDateTime now = LocalDateTime.now();
        authToken.setCreatedAt(now);
        authToken.setExpiresAt(now.plusMinutes(10));
        authToken.setUsed(false);

        assertTrue(authToken.isValid());
    }

    /**
     * Tests isValid method when token has been used
     * Expected: Returns false for used tokens even if not expired
     */
    @Test
    public void testIsValid_WhenTokenIsUsed() {
        // Set expiration to future but mark as used
        LocalDateTime now = LocalDateTime.now();
        authToken.setCreatedAt(now);
        authToken.setExpiresAt(now.plusMinutes(10));
        authToken.setUsed(true);

        assertFalse(authToken.isValid());
    }

    /**
     * Tests isValid method when token is expired
     * Expected: Returns false for expired tokens even if not used
     */
    @Test
    public void testIsValid_WhenTokenIsExpired() {
        // Set token as not used but expired
        LocalDateTime now = LocalDateTime.now();
        authToken.setCreatedAt(now.minusMinutes(10));
        authToken.setExpiresAt(now.minusMinutes(5));
        authToken.setUsed(false);

        assertFalse(authToken.isValid());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        LocalDateTime newCreated = LocalDateTime.now().minusMinutes(5);
        LocalDateTime newExpires = LocalDateTime.now().plusMinutes(10);

        authToken.setId(99L);
        authToken.setToken("updated-token");
        authToken.setCreatedAt(newCreated);
        authToken.setExpiresAt(newExpires);
        authToken.setUsed(true);

        assertEquals(99L, authToken.getId());
        assertEquals("updated-token", authToken.getToken());
        assertEquals(newCreated, authToken.getCreatedAt());
        assertEquals(newExpires, authToken.getExpiresAt());
        assertTrue(authToken.isUsed());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create another instance with same values
        AuthToken sameToken = new AuthToken();
        sameToken.setId(null);
        sameToken.setToken("test-token-1234");
        sameToken.setCreatedAt(authToken.getCreatedAt());
        sameToken.setExpiresAt(authToken.getExpiresAt());
        sameToken.setUsed(false);
        sameToken.setClient(mockClient);

        // Test equality with same and similar objects
        assertEquals(authToken, authToken); // Reflexivity - same instance
        assertEquals(authToken, sameToken); // Different instance but same values

        // Test inequality with different objects
        AuthToken differentToken = new AuthToken();
        differentToken.setToken("different-token");

        assertNotEquals(authToken, differentToken);
        assertNotEquals(authToken, null);
        assertNotEquals(authToken, new Object());

        // Test hashCode consistency with equals
        assertEquals(authToken.hashCode(), sameToken.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(authToken.hashCode(), differentToken.hashCode()); // Different objects have different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains essential field values
     */
    @Test
    public void testToString() {
        String toString = authToken.toString();

        // Verify toString contains essential fields
        assertTrue(toString.contains("test-token-1234"));
        assertTrue(toString.contains("isUsed=false"));
    }
}