package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CardService
 * Focuses on testing card verification process and related error handling
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @InjectMocks
    private CardService cardService;

    // Mock dependencies
    @Mock private PaymentRequestDTO mockPaymentRequest;
    @Mock private Client mockClient;
    @Mock private PrivateKey mockPrivateKey;

    // Static mocks
    private static MockedStatic<KeystoreUtils> keystoreUtilsMockedStatic;
    private static MockedStatic<SignUtils> signUtilsMockedStatic;
    private static MockedStatic<SslUtils> sslUtilsMockedStatic;

    // Test constants
    private final String DUMMY_CARD_NUMBER = "9876543210987654";
    private final String DUMMY_EXP_DATE = "11/2026";
    private final BigDecimal DUMMY_AMOUNT = new BigDecimal("12.34");
    private final String DUMMY_MERCHANT = "TestMart";
    private final String DUMMY_PAYMENT_ID = "attempt-123";
    private final String DUMMY_CLIENT_SIGNATURE = "clientSignedData";

    @BeforeEach
    void setUp() {
        // Clean up any existing static mocks
        closeStaticMocks();

        // Initialize static mocks
        keystoreUtilsMockedStatic = mockStatic(KeystoreUtils.class);
        signUtilsMockedStatic = mockStatic(SignUtils.class);
        sslUtilsMockedStatic = mockStatic(SslUtils.class);

        // Inject @Value properties required by CardService
        ReflectionTestUtils.setField(cardService, "acsPort", 8081);
        ReflectionTestUtils.setField(cardService, "clientKeystorePath", "dummy/client_keystore.jks");
        ReflectionTestUtils.setField(cardService, "clientKeystorePassword", "ksPass");
        ReflectionTestUtils.setField(cardService, "clientKeystoreAlias", "clientAlias");
        ReflectionTestUtils.setField(cardService, "clientKeyPassword", "keyPass");
        ReflectionTestUtils.setField(cardService, "clientTruststorePathForAcs", "dummy/client_truststore.jks");
        ReflectionTestUtils.setField(cardService, "clientTruststorePasswordForAcs", "tsPass");
        ReflectionTestUtils.setField(cardService, "acsTrustedAlias", "acsAlias");
    }

    @AfterEach
    void tearDown() {
        closeStaticMocks();
    }

    /**
     * Helper method to safely close all static mocks
     */
    private void closeStaticMocks() {
        if (keystoreUtilsMockedStatic != null && !keystoreUtilsMockedStatic.isClosed()) keystoreUtilsMockedStatic.close();
        if (signUtilsMockedStatic != null && !signUtilsMockedStatic.isClosed()) signUtilsMockedStatic.close();
        if (sslUtilsMockedStatic != null && !sslUtilsMockedStatic.isClosed()) sslUtilsMockedStatic.close();
    }

    /**
     * Tests scenario where retrieving the private key fails
     * Expected: Returns null without attempting signature or SSL connection
     */
    @Test
    void initiateCardVerification_GetPrivateKeyFails_ReturnsNull() throws Exception {
        // Arrange - Set up required mock data
        when(mockClient.getEmail()).thenReturn("client@test.com");
        when(mockPaymentRequest.getCardNumber()).thenReturn(DUMMY_CARD_NUMBER);
        when(mockPaymentRequest.getExpirationDate()).thenReturn(DUMMY_EXP_DATE);
        when(mockPaymentRequest.getAmount()).thenReturn(DUMMY_AMOUNT);
        when(mockPaymentRequest.getMerchantName()).thenReturn(DUMMY_MERCHANT);

        // Configure failure in keystore access
        keystoreUtilsMockedStatic.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Keystore error"));

        // Act
        String resultOtp = cardService.initiateCardVerification(mockPaymentRequest, mockClient, DUMMY_PAYMENT_ID);

        // Assert
        assertNull(resultOtp);

        // Verify - Ensure subsequent operations were not attempted
        signUtilsMockedStatic.verify(() -> SignUtils.signData(anyString(), any()), never());
        sslUtilsMockedStatic.verify(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()), never());
    }

    /**
     * Tests scenario where signing the data fails
     * Expected: Returns null without attempting SSL connection
     */
    @Test
    void initiateCardVerification_SignDataFails_ReturnsNull() throws Exception {
        // Arrange - Set up required mock data
        when(mockClient.getEmail()).thenReturn("client@test.com");
        when(mockPaymentRequest.getCardNumber()).thenReturn(DUMMY_CARD_NUMBER);
        when(mockPaymentRequest.getExpirationDate()).thenReturn(DUMMY_EXP_DATE);
        when(mockPaymentRequest.getAmount()).thenReturn(DUMMY_AMOUNT);
        when(mockPaymentRequest.getMerchantName()).thenReturn(DUMMY_MERCHANT);

        // KeystoreUtils returns a valid key but SignUtils fails
        keystoreUtilsMockedStatic.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockPrivateKey);
        signUtilsMockedStatic.when(() -> SignUtils.signData(anyString(), eq(mockPrivateKey)))
                .thenThrow(new RuntimeException("Signing error"));

        // Act
        String resultOtp = cardService.initiateCardVerification(mockPaymentRequest, mockClient, DUMMY_PAYMENT_ID);

        // Assert
        assertNull(resultOtp);

        // Verify - Ensure SSL connection was not attempted
        sslUtilsMockedStatic.verify(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()), never());
    }

    /**
     * Tests scenario where establishing SSL connection to ACS server fails
     * Expected: Returns null after attempting SSL connection
     */
    @Test
    void initiateCardVerification_SendToAcsFails_ReturnsNull() throws Exception {
        // Arrange - Set up required mock data
        when(mockClient.getEmail()).thenReturn("client@test.com");
        when(mockPaymentRequest.getCardNumber()).thenReturn(DUMMY_CARD_NUMBER);
        when(mockPaymentRequest.getExpirationDate()).thenReturn(DUMMY_EXP_DATE);
        when(mockPaymentRequest.getAmount()).thenReturn(DUMMY_AMOUNT);
        when(mockPaymentRequest.getMerchantName()).thenReturn(DUMMY_MERCHANT);

        // Configure successful key retrieval and signing
        keystoreUtilsMockedStatic.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockPrivateKey);
        signUtilsMockedStatic.when(() -> SignUtils.signData(anyString(), eq(mockPrivateKey)))
                .thenReturn(DUMMY_CLIENT_SIGNATURE);

        // Configure SSL connection failure
        sslUtilsMockedStatic.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                .thenThrow(new IOException("Connection refused"));

        // Act
        String resultOtp = cardService.initiateCardVerification(mockPaymentRequest, mockClient, DUMMY_PAYMENT_ID);

        // Assert
        assertNull(resultOtp);

        // Verify - Ensure SSL socket creation was attempted
        sslUtilsMockedStatic.verify(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()), times(1));
    }
}