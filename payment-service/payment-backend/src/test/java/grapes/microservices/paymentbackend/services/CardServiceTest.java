package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.SSLSocket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @InjectMocks
    private CardService cardService;

    private PaymentRequestDTO paymentRequest;
    private Client client;
    private String paymentAttemptId;

    @BeforeEach
    void setUp() {
        // Initialize test data
        paymentRequest = new PaymentRequestDTO();
        paymentRequest.setCardNumber("4111111111111111");
        paymentRequest.setExpirationDate("12/25");
        paymentRequest.setAmount(BigDecimal.valueOf(100.0));
        paymentRequest.setMerchantName("Test Merchant");

        client = new Client();
        client.setId(1L);
        client.setEmail("test@example.com");
        client.setPhoneNumber("+15555555555");

        paymentAttemptId = "test-payment-123";

        // Set properties through reflection (normally set by @Value)
        ReflectionTestUtils.setField(cardService, "acsPort", 8443);
        ReflectionTestUtils.setField(cardService, "clientKeystorePath", "client.keystore");
        ReflectionTestUtils.setField(cardService, "clientKeystorePassword", "password");
        ReflectionTestUtils.setField(cardService, "clientKeystoreAlias", "client");
        ReflectionTestUtils.setField(cardService, "clientKeyPassword", "keypassword");
        ReflectionTestUtils.setField(cardService, "clientTruststorePathForAcs", "client.truststore");
        ReflectionTestUtils.setField(cardService, "clientTruststorePasswordForAcs", "trustpass");
        ReflectionTestUtils.setField(cardService, "acsTrustedAlias", "acs_trusted");
    }

    @Test
    void initiateCardVerification_Success() throws Exception {
        // Arrange
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        Certificate mockCertificate = mock(Certificate.class);
        PublicKey mockPublicKey = mock(PublicKey.class);
        SSLSocket mockSslSocket = mock(SSLSocket.class);

        // Set up input/output streams for the socket
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        String mockResponse = "source=acs&data=123456&signature=validSignature";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());

        // Setup parsed response
        Map<String, String> parsedResponse = new HashMap<>();
        parsedResponse.put("source", "acs");
        parsedResponse.put("data", "123456");
        parsedResponse.put("signature", "validSignature");

        try (MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class);
             MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class)) {

            // Mock the dependencies
            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);
            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSslSocket);

            when(mockSslSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSslSocket.getInputStream()).thenReturn(inputStream);

            dataUtilsMock.when(() -> DataUtils.parseData(anyString())).thenReturn(parsedResponse);

            keystoreUtilsMock.when(() -> KeystoreUtils.getCertificate(anyString(), anyString(), anyString()))
                    .thenReturn(mockCertificate);
            when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);

            signUtilsMock.when(() -> SignUtils.verifySignature(anyString(), anyString(), any(PublicKey.class)))
                    .thenReturn(true);

            // Act
            String result = cardService.initiateCardVerification(paymentRequest, client, paymentAttemptId);

            // Assert
            assertEquals("123456", result);
            verify(mockSslSocket).getOutputStream();
            verify(mockSslSocket).getInputStream();
        }
    }

    @Test
    void initiateCardVerification_InvalidAcsResponse() throws Exception {
        // Arrange
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        SSLSocket mockSslSocket = mock(SSLSocket.class);

        // Set up input/output streams for the socket
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        String mockResponse = "source=unknown&data=123456&signature=validSignature";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());

        // Setup parsed response
        Map<String, String> parsedResponse = new HashMap<>();
        parsedResponse.put("source", "unknown"); // Invalid source
        parsedResponse.put("data", "123456");
        parsedResponse.put("signature", "validSignature");

        try (MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class);
             MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class)) {

            // Mock the dependencies
            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);
            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSslSocket);

            when(mockSslSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSslSocket.getInputStream()).thenReturn(inputStream);

            dataUtilsMock.when(() -> DataUtils.parseData(anyString())).thenReturn(parsedResponse);

            // Act
            String result = cardService.initiateCardVerification(paymentRequest, client, paymentAttemptId);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void initiateCardVerification_InvalidSignature() throws Exception {
        // Arrange
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        Certificate mockCertificate = mock(Certificate.class);
        PublicKey mockPublicKey = mock(PublicKey.class);
        SSLSocket mockSslSocket = mock(SSLSocket.class);

        // Set up input/output streams for the socket
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        String mockResponse = "source=acs&data=123456&signature=invalidSignature";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());

        // Setup parsed response
        Map<String, String> parsedResponse = new HashMap<>();
        parsedResponse.put("source", "acs");
        parsedResponse.put("data", "123456");
        parsedResponse.put("signature", "invalidSignature");

        try (MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class);
             MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class)) {

            // Mock the dependencies
            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);
            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSslSocket);

            when(mockSslSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSslSocket.getInputStream()).thenReturn(inputStream);

            dataUtilsMock.when(() -> DataUtils.parseData(anyString())).thenReturn(parsedResponse);

            keystoreUtilsMock.when(() -> KeystoreUtils.getCertificate(anyString(), anyString(), anyString()))
                    .thenReturn(mockCertificate);
            when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);

            signUtilsMock.when(() -> SignUtils.verifySignature(anyString(), anyString(), any(PublicKey.class)))
                    .thenReturn(false); // Invalid signature

            // Act
            String result = cardService.initiateCardVerification(paymentRequest, client, paymentAttemptId);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void initiateCardVerification_ErrorResponse() throws Exception {
        // Arrange
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        SSLSocket mockSslSocket = mock(SSLSocket.class);

        // Set up input/output streams for the socket with an error response
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        String mockResponse = "ERROR:Some error message";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());

        try (MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class)) {

            // Mock the dependencies
            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);
            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSslSocket);

            when(mockSslSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSslSocket.getInputStream()).thenReturn(inputStream);

            // Act
            String result = cardService.initiateCardVerification(paymentRequest, client, paymentAttemptId);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void initiateCardVerification_CommunicationException() throws Exception {
        // Arrange
        PrivateKey mockPrivateKey = mock(PrivateKey.class);

        try (MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class)) {

            // Mock the dependencies
            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);
            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            // Simulate SSL connection failure
            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Connection failed"));

            // Act
            String result = cardService.initiateCardVerification(paymentRequest, client, paymentAttemptId);

            // Assert
            assertNull(result);
        }
    }
}