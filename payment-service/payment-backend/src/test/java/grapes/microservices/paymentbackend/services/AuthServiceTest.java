package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthTokenRepository tokenRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<AuthToken> tokenCaptor;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setEmail("test@example.com");
        testClient.setPhoneNumber("+15555555555");
    }

    @Test
    void generateOtp_ShouldReturn6DigitCode() {
        // Act
        String otp = authService.generateOtp();

        // Assert
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void createToken_WithValidClient_ShouldCreateAndSendOtp() {
        // Arrange
        when(tokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(smsService).sendOtp(anyString(), anyString());

        // Act
        AuthToken result = authService.createToken(testClient);

        // Assert
        assertNotNull(result);
        assertEquals(testClient, result.getClient());
        assertNotNull(result.getToken());
        assertEquals(6, result.getToken().length());

        // Verify SMS was sent
        verify(smsService).sendOtp(eq("+15555555555"), anyString());
        verify(tokenRepository).save(any(AuthToken.class));
    }

    @Test
    void createToken_WithMissingPhoneNumber_ShouldThrowException() {
        // Arrange
        testClient.setPhoneNumber(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.createToken(testClient));
        assertEquals("Client doesn't have a registered phone number", exception.getMessage());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void createToken_WhenSmsFails_ShouldThrowException() {
        // Arrange
        doThrow(new RuntimeException("SMS service unavailable"))
                .when(smsService).sendOtp(anyString(), anyString());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.createToken(testClient));
        assertEquals("Failed to send OTP via SMS", exception.getMessage());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void saveToken_ShouldDelegateToRepository() {
        // Arrange
        AuthToken token = new AuthToken("123456", testClient);
        when(tokenRepository.save(token)).thenReturn(token);

        // Act
        AuthToken result = authService.saveToken(token);

        // Assert
        assertSame(token, result);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyToken_WithValidToken_ShouldReturnTrueAndMarkAsUsed() {
        // Arrange
        String tokenValue = "123456";
        AuthToken token = new AuthToken(tokenValue, testClient);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        when(tokenRepository.findByTokenAndClient(tokenValue, testClient)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(AuthToken.class))).thenReturn(token);

        // Act
        boolean result = authService.verifyToken(tokenValue, testClient);

        // Assert
        assertTrue(result);
        verify(tokenRepository).save(tokenCaptor.capture());
        AuthToken savedToken = tokenCaptor.getValue();
        assertTrue(savedToken.isUsed());
    }

    @Test
    void verifyToken_WithNonExistentToken_ShouldReturnFalse() {
        // Arrange
        String tokenValue = "123456";
        when(tokenRepository.findByTokenAndClient(tokenValue, testClient)).thenReturn(Optional.empty());

        // Act
        boolean result = authService.verifyToken(tokenValue, testClient);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void verifyToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        String tokenValue = "123456";
        AuthToken token = new AuthToken(tokenValue, testClient);
        token.setCreatedAt(LocalDateTime.now().minusHours(1));
        token.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Expired
        token.setUsed(false);

        when(tokenRepository.findByTokenAndClient(tokenValue, testClient)).thenReturn(Optional.of(token));

        // Act
        boolean result = authService.verifyToken(tokenValue, testClient);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void verifyToken_WithUsedToken_ShouldReturnFalse() {
        // Arrange
        String tokenValue = "123456";
        AuthToken token = new AuthToken(tokenValue, testClient);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(true); // Already used

        when(tokenRepository.findByTokenAndClient(tokenValue, testClient)).thenReturn(Optional.of(token));

        // Act
        boolean result = authService.verifyToken(tokenValue, testClient);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void getLastToken_ShouldDelegateToRepository() {
        // Arrange
        AuthToken token = new AuthToken("123456", testClient);
        when(tokenRepository.findFirstByClientOrderByCreatedAtDesc(testClient)).thenReturn(Optional.of(token));

        // Act
        Optional<AuthToken> result = authService.getLastToken(testClient);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(token, result.get());
        verify(tokenRepository).findFirstByClientOrderByCreatedAtDesc(testClient);
    }
}