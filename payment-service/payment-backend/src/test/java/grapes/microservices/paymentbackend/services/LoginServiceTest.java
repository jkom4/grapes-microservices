package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.LoginRequest;
import grapes.microservices.paymentbackend.dto.LoginResponse;
import grapes.microservices.paymentbackend.dto.PaymentInitiateRequest;
import grapes.microservices.paymentbackend.models.Client;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginService loginService;

    private Client testClient;
    private LoginRequest validLoginRequest;
    private PaymentInitiateRequest validPaymentRequest;

    @BeforeEach
    void setUp() {
        // Set up test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setEmail("test@example.com");
        testClient.setPassword("hashedPassword");

        // Set up valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Set up valid payment initiate request
        validPaymentRequest = new PaymentInitiateRequest();
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setMerchantId("MERCHANT123");
    }

    @Test
    void initiatePayment_ShouldStoreDetailsInSessionAndReturnSuccess() {
        // Arrange
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");

        // Act
        Map<String, Object> result = loginService.initiatePayment(validPaymentRequest, request);

        // Assert
        assertEquals("success", result.get("status"));
        assertEquals("Payment context created. Redirecting to login page.", result.get("message"));
        assertEquals("http://localhost:3000/login", result.get("redirectUrl"));

        // Verify session attributes were set
        verify(session).setAttribute("initialPaymentAmount", validPaymentRequest.getAmount());
        verify(session).setAttribute("initialMerchantName", validPaymentRequest.getMerchantId());
        verify(session).setAttribute(eq("initialPaymentId"), anyString()); // UUID is random
        verify(request).getSession(true);
    }

    @Test
    void initiatePayment_ShouldThrowException_WhenAmountIsNull() {
        // Arrange
        validPaymentRequest.setAmount(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loginService.initiatePayment(validPaymentRequest, request));
        assertEquals("Amount and merchantId (merchant identifier) are required", exception.getMessage());

        // Verify session was not created
        verify(request, never()).getSession(true);
    }

    @Test
    void initiatePayment_ShouldThrowException_WhenMerchantIdIsNull() {
        // Arrange
        validPaymentRequest.setMerchantId(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loginService.initiatePayment(validPaymentRequest, request));
        assertEquals("Amount and merchantId (merchant identifier) are required", exception.getMessage());

        // Verify session was not created
        verify(request, never()).getSession(true);
    }

    @Test
    void initiatePayment_ShouldThrowException_WhenMerchantIdIsEmpty() {
        // Arrange
        validPaymentRequest.setMerchantId("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loginService.initiatePayment(validPaymentRequest, request));
        assertEquals("Amount and merchantId (merchant identifier) are required", exception.getMessage());

        // Verify session was not created
        verify(request, never()).getSession(true);
    }

    @Test
    void login_ShouldAuthenticateAndReturnSuccessResponse_WhenCredentialsAreValid() {
        // Arrange
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");
        when(clientService.verifyCredentials("test@example.com", "password123")).thenReturn(true);
        when(clientService.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // Act
        LoginResponse result = loginService.login(validLoginRequest, request);

        // Assert
        assertEquals("test-session-id", result.getToken());
        assertEquals("success", result.getStatus());
        assertEquals("Client authenticated successfully", result.getMessage());

        // Verify session attributes
        verify(session).setAttribute("clientId", 1L);
        verify(session).setAttribute("clientEmail", "test@example.com");
        verify(request).getSession(true);
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Arrange
        when(clientService.verifyCredentials("test@example.com", "password123")).thenReturn(false);

        HttpSession existingSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(existingSession);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loginService.login(validLoginRequest, request));
        assertEquals("Invalid credentials", exception.getMessage());

        // Verify session invalidation
        verify(existingSession).invalidate();
        verify(clientService, never()).findByEmail(anyString());
    }



    @Test
    void login_ShouldHandleExistingPaymentInformation() {
        // Arrange
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");
        when(clientService.verifyCredentials("test@example.com", "password123")).thenReturn(true);
        when(clientService.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        BigDecimal initialAmount = new BigDecimal("250.00");
        when(session.getAttribute("initialPaymentAmount")).thenReturn(initialAmount);

        // Act
        LoginResponse result = loginService.login(validLoginRequest, request);

        // Assert
        assertEquals("test-session-id", result.getToken());
        assertEquals("success", result.getStatus());

        // Verify session attributes
        verify(session).setAttribute("clientId", 1L);
        verify(session).setAttribute("clientEmail", "test@example.com");
        verify(session).getAttribute("initialPaymentAmount");
    }

    @Test
    void login_ShouldInvalidateExistingSession_WhenAuthenticationFails() {
        // Arrange
        when(clientService.verifyCredentials("test@example.com", "password123")).thenReturn(false);

        HttpSession existingSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(existingSession);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> loginService.login(validLoginRequest, request));

        // Verify existing session was invalidated
        verify(existingSession).invalidate();
    }

    @Test
    void login_ShouldNotInvalidateNonExistentSession_WhenAuthenticationFails() {
        // Arrange
        when(clientService.verifyCredentials("test@example.com", "password123")).thenReturn(false);
        when(request.getSession(false)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> loginService.login(validLoginRequest, request));

        // Verify we checked for existing session but didn't try to invalidate anything
        verify(request).getSession(false);
    }
}