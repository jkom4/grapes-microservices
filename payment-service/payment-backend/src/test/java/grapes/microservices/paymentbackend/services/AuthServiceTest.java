package grapes.microservices.paymentbackend.services;
import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import grapes.microservices.paymentbackend.services.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthService
 * Tests OTP generation, token creation, verification, and management
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthTokenRepository tokenRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private Client mockClient;

    @Mock
    private AuthToken mockAuthToken;

    @Captor
    private ArgumentCaptor<AuthToken> authTokenCaptor;

    // Constants for validation and testing
    private static final Pattern OTP_PATTERN = Pattern.compile("\\d{6}");
    private final String VALID_PHONE_NUMBER = "+1234567890";
    private final String TEST_EMAIL = "test@example.com";
    private final Long CLIENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // No global mock setup needed
    }

    /**
     * Tests that the OTP generation creates valid 6-digit numeric codes
     * and that consecutive generations produce different values
     */
    @Test
    void generateOtp_ReturnsSixDigitString() {
        // Act - Generate two OTPs to verify randomness
        String otp1 = authService.generateOtp();
        String otp2 = authService.generateOtp();

        // Assert - Verify format and uniqueness
        assertNotNull(otp1);
        assertEquals(6, otp1.length());
        assertTrue(OTP_PATTERN.matcher(otp1).matches(), "OTP should contain only digits");
        assertNotNull(otp2);
        assertEquals(6, otp2.length());
        assertTrue(OTP_PATTERN.matcher(otp2).matches(), "OTP should contain only digits");
        assertNotEquals(otp1, otp2, "Consecutive OTPs should likely be different");
    }

    /**
     * Tests successful token creation when a client has a valid phone number
     * Verifies that SMS is sent and token is saved
     */
    @Test
    void createToken_ClientWithPhoneNumber_SendsSmsAndSavesToken() {
        // Arrange
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockClient.getPhoneNumber()).thenReturn(VALID_PHONE_NUMBER);
        when(tokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(smsService).sendOtp(anyString(), anyString());

        // Act
        AuthToken createdToken = authService.createToken(mockClient);

        // Assert
        assertNotNull(createdToken);
        assertNotNull(createdToken.getToken());
        assertEquals(6, createdToken.getToken().length());
        assertEquals(mockClient, createdToken.getClient());
        assertFalse(createdToken.isUsed());

        // Verify
        verify(smsService, times(1)).sendOtp(eq(VALID_PHONE_NUMBER), eq(createdToken.getToken()));
        verify(tokenRepository, times(1)).save(authTokenCaptor.capture());
        AuthToken savedToken = authTokenCaptor.getValue();
        assertEquals(createdToken.getToken(), savedToken.getToken());
        assertEquals(mockClient, savedToken.getClient());
    }

    /**
     * Tests token creation failure when client has no phone number
     * Expected: IllegalStateException
     */
    @Test
    void createToken_ClientWithoutPhoneNumber_ThrowsIllegalStateException() {
        // Arrange
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockClient.getPhoneNumber()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.createToken(mockClient);
        });
        assertEquals("Client doesn't have a registered phone number", exception.getMessage());

        // Verify - Check no SMS was sent and no token was saved
        verify(mockClient, atLeastOnce()).getPhoneNumber();
        verify(mockClient, times(2)).getEmail();
        verifyNoInteractions(smsService);
        verifyNoInteractions(tokenRepository);
    }

    /**
     * Tests token creation failure when client has empty phone number
     * Expected: IllegalStateException
     */
    @Test
    void createToken_ClientWithEmptyPhoneNumber_ThrowsIllegalStateException() {
        // Arrange
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockClient.getPhoneNumber()).thenReturn("");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.createToken(mockClient);
        });
        assertEquals("Client doesn't have a registered phone number", exception.getMessage());

        // Verify - Check no SMS was sent and no token was saved
        verify(mockClient, atLeastOnce()).getPhoneNumber();
        verify(mockClient, times(2)).getEmail();
        verifyNoInteractions(smsService);
        verifyNoInteractions(tokenRepository);
    }

    /**
     * Tests token creation when SMS service fails
     * Expected: IllegalStateException with original exception as cause
     */
    @Test
    void createToken_SmsServiceThrowsException_ThrowsIllegalStateException() {
        // Arrange
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockClient.getPhoneNumber()).thenReturn(VALID_PHONE_NUMBER);
        doThrow(new RuntimeException("SMS Gateway Down"))
                .when(smsService).sendOtp(eq(VALID_PHONE_NUMBER), anyString());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.createToken(mockClient);
        });
        assertEquals("Failed to send OTP via SMS", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("SMS Gateway Down", exception.getCause().getMessage());

        // Verify - Check SMS was attempted but no token was saved
        verify(smsService, times(1)).sendOtp(eq(VALID_PHONE_NUMBER), anyString());
        verifyNoInteractions(tokenRepository);
    }

    /**
     * Tests that saveToken properly calls the repository
     */
    @Test
    void saveToken_CallsRepositorySave() {
        // Arrange
        when(mockAuthToken.getToken()).thenReturn("abcdef");
        when(mockAuthToken.getClient()).thenReturn(mockClient);
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(tokenRepository.save(mockAuthToken)).thenReturn(mockAuthToken);

        // Act
        AuthToken savedToken = authService.saveToken(mockAuthToken);

        // Assert
        assertNotNull(savedToken);
        assertEquals(mockAuthToken, savedToken);
        verify(tokenRepository, times(1)).save(eq(mockAuthToken));
    }

    /**
     * Tests successful token verification
     * Expected: Returns true and marks token as used
     */
    @Test
    void verifyToken_ValidToken_ReturnsTrueAndMarksAsUsed() {
        // Arrange
        String tokenValue = "112233";
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockAuthToken.isValid()).thenReturn(true);
        when(tokenRepository.findByTokenAndClient(tokenValue, mockClient)).thenReturn(Optional.of(mockAuthToken));

        // Act
        boolean result = authService.verifyToken(tokenValue, mockClient);

        // Assert
        assertTrue(result);
        verify(tokenRepository, times(1)).findByTokenAndClient(eq(tokenValue), eq(mockClient));
        verify(mockAuthToken, times(1)).isValid();
        verify(mockAuthToken, times(1)).setUsed(true);
        verify(tokenRepository, times(1)).save(eq(mockAuthToken));
    }

    /**
     * Tests token verification when token is not found
     * Expected: Returns false
     */
    @Test
    void verifyToken_TokenNotFound_ReturnsFalse() {
        // Arrange
        String tokenValue = "112233";
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(tokenRepository.findByTokenAndClient(tokenValue, mockClient)).thenReturn(Optional.empty());

        // Act
        boolean result = authService.verifyToken(tokenValue, mockClient);

        // Assert
        assertFalse(result);
        verify(tokenRepository, times(1)).findByTokenAndClient(eq(tokenValue), eq(mockClient));
        verifyNoInteractions(mockAuthToken);
        verify(tokenRepository, never()).save(any(AuthToken.class));
    }

    /**
     * Tests token verification when token is invalid (expired)
     * Expected: Returns false
     */
    @Test
    void verifyToken_TokenNotValid_ReturnsFalse() {
        // Arrange
        String tokenValue = "112233";
        when(mockClient.getEmail()).thenReturn(TEST_EMAIL);
        when(mockAuthToken.isValid()).thenReturn(false);
        when(tokenRepository.findByTokenAndClient(tokenValue, mockClient)).thenReturn(Optional.of(mockAuthToken));

        // Act
        boolean result = authService.verifyToken(tokenValue, mockClient);

        // Assert
        assertFalse(result);
        verify(tokenRepository, times(1)).findByTokenAndClient(eq(tokenValue), eq(mockClient));
        verify(mockAuthToken, times(1)).isValid();
        verify(mockAuthToken, never()).setUsed(anyBoolean());
        verify(tokenRepository, never()).save(any(AuthToken.class));
    }

    /**
     * Tests retrieval of the most recent token for a client
     */
    @Test
    void getLastToken_CallsRepository() {
        // Arrange
        Optional<AuthToken> expectedOptional = Optional.of(mockAuthToken);
        when(tokenRepository.findFirstByClientOrderByCreatedAtDesc(mockClient)).thenReturn(expectedOptional);

        // Act
        Optional<AuthToken> resultOptional = authService.getLastToken(mockClient);

        // Assert
        assertEquals(expectedOptional, resultOptional);
        verify(tokenRepository, times(1)).findFirstByClientOrderByCreatedAtDesc(eq(mockClient));
    }

    /**
     * Tests retrieval of the most recent token when client has no tokens
     * Expected: Returns empty Optional
     */
    @Test
    void getLastToken_ClientHasNoTokens_ReturnsEmptyOptional() {
        // Arrange
        Optional<AuthToken> expectedOptional = Optional.empty();
        when(tokenRepository.findFirstByClientOrderByCreatedAtDesc(mockClient)).thenReturn(expectedOptional);

        // Act
        Optional<AuthToken> resultOptional = authService.getLastToken(mockClient);

        // Assert
        assertTrue(resultOptional.isEmpty());
        verify(tokenRepository, times(1)).findFirstByClientOrderByCreatedAtDesc(eq(mockClient));
    }
}