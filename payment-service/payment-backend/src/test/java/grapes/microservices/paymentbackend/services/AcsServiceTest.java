package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcsServiceTest {

    @InjectMocks
    private AcsService acsService;

    @Mock
    private SSLSocket mockSslSocket;

    @Mock
    private PrivateKey mockPrivateKey;

    @Mock
    private Certificate mockCertificate;

    @Mock
    private PublicKey mockPublicKey;

    @BeforeEach
    void setUp() {
        // Set values for the properties through reflection
        ReflectionTestUtils.setField(acsService, "acsPort", 8443);
        ReflectionTestUtils.setField(acsService, "clientKeystorePath", "client.keystore");
        ReflectionTestUtils.setField(acsService, "clientKeystorePassword", "password");
        ReflectionTestUtils.setField(acsService, "clientKeystoreAlias", "client");
        ReflectionTestUtils.setField(acsService, "clientKeyPassword", "keypassword");
        ReflectionTestUtils.setField(acsService, "clientTruststorePathForAcs", "client.truststore");
        ReflectionTestUtils.setField(acsService, "clientTruststorePasswordForAcs", "trustpass");
        ReflectionTestUtils.setField(acsService, "acsTrustedAlias", "acs_trusted");
    }

    @Test
    void processPayment_Success() throws Exception {
        // Arrange
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransferAmount(BigDecimal.valueOf(100.0));
        transaction.setMerchantName("TestMerchant");

        Card card = new Card();
        card.setCardNumber("4111111111111111");
        card.setExpirationDate("12/25");

        Client client = new Client();
        client.setEmail("test@example.com");
        client.setCards(Collections.singletonList(card));

        // Mock the SSL socket creation
        try (MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class);
             MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class);
             MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class);
             MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class)) {

            // Setup mocks
            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSslSocket);

            keystoreUtilsMock.when(() -> KeystoreUtils.getPrivateKey(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);

            keystoreUtilsMock.when(() -> KeystoreUtils.getCertificate(
                            anyString(), anyString(), anyString()))
                    .thenReturn(mockCertificate);

            when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);

            signUtilsMock.when(() -> SignUtils.signData(anyString(), any(PrivateKey.class)))
                    .thenReturn("mockedSignature");

            signUtilsMock.when(() -> SignUtils.verifySignature(anyString(), anyString(), any(PublicKey.class)))
                    .thenReturn(true);

            // Mock SSL socket I/O
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            when(mockSslSocket.getOutputStream()).thenReturn(outputStream);

            String mockResponse = "source=acs&data=123456&signature=validSignature";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());
            when(mockSslSocket.getInputStream()).thenReturn(inputStream);

            // Mock data parsing
            Map<String, String> parsedResponse = new HashMap<>();
            parsedResponse.put("source", "acs");
            parsedResponse.put("data", "123456");
            parsedResponse.put("signature", "validSignature");
            dataUtilsMock.when(() -> DataUtils.parseData(anyString())).thenReturn(parsedResponse);

            // Act
            boolean result = acsService.processPayment(transaction, client);

            // Assert
            assertTrue(result);
            verify(mockSslSocket).getOutputStream();
            verify(mockSslSocket).getInputStream();
        }
    }

    @Test
    void processPayment_MissingCardDetails() {
        // Arrange
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransferAmount(BigDecimal.valueOf(100.0));
        transaction.setMerchantName("TestMerchant");

        Client client = new Client();
        client.setEmail("test@example.com");
        // No cards set

        // Act
        boolean result = acsService.processPayment(transaction, client);

        // Assert
        assertFalse(result);
    }

    @Test
    void processAcsResponse_ValidResponse() throws Exception {
        // Arrange
        String validAcsResponse = "source=acs&data=123456&signature=validSignature";

        try (MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class);
             MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class)) {

            Map<String, String> parsedResponse = new HashMap<>();
            parsedResponse.put("source", "acs");
            parsedResponse.put("data", "123456");
            parsedResponse.put("signature", "validSignature");
            dataUtilsMock.when(() -> DataUtils.parseData(validAcsResponse)).thenReturn(parsedResponse);

            keystoreUtilsMock.when(() -> KeystoreUtils.getCertificate(
                            anyString(), anyString(), anyString()))
                    .thenReturn(mockCertificate);

            when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);

            try (MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class)) {
                signUtilsMock.when(() -> SignUtils.verifySignature("123456", "validSignature", mockPublicKey))
                        .thenReturn(true);

                // Act
                boolean result = acsService.processAcsResponse(validAcsResponse);

                // Assert
                assertTrue(result);
            }
        }
    }

    @Test
    void processAcsResponse_InvalidResponse() throws Exception {
        // Arrange
        String invalidAcsResponse = "source=unknown&data=123456&signature=validSignature";

        try (MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class)) {
            Map<String, String> parsedResponse = new HashMap<>();
            parsedResponse.put("source", "unknown"); // Invalid source
            parsedResponse.put("data", "123456");
            parsedResponse.put("signature", "validSignature");
            dataUtilsMock.when(() -> DataUtils.parseData(invalidAcsResponse)).thenReturn(parsedResponse);

            // Act
            boolean result = acsService.processAcsResponse(invalidAcsResponse);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void processAcsResponse_InvalidSignature() throws Exception {
        // Arrange
        String validAcsResponse = "source=acs&data=123456&signature=invalidSignature";

        try (MockedStatic<DataUtils> dataUtilsMock = mockStatic(DataUtils.class);
             MockedStatic<KeystoreUtils> keystoreUtilsMock = mockStatic(KeystoreUtils.class)) {

            Map<String, String> parsedResponse = new HashMap<>();
            parsedResponse.put("source", "acs");
            parsedResponse.put("data", "123456");
            parsedResponse.put("signature", "invalidSignature");
            dataUtilsMock.when(() -> DataUtils.parseData(validAcsResponse)).thenReturn(parsedResponse);

            keystoreUtilsMock.when(() -> KeystoreUtils.getCertificate(
                            anyString(), anyString(), anyString()))
                    .thenReturn(mockCertificate);

            when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);

            try (MockedStatic<SignUtils> signUtilsMock = mockStatic(SignUtils.class)) {
                signUtilsMock.when(() -> SignUtils.verifySignature("123456", "invalidSignature", mockPublicKey))
                        .thenReturn(false);

                // Act
                boolean result = acsService.processAcsResponse(validAcsResponse);

                // Assert
                assertFalse(result);
            }
        }
    }
}