package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentService paymentService;

    private Client testClient;
    private Card testCard;
    private PaymentRequestDTO validPaymentRequest;
    private TransactionEntity testTransaction;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setEmail("test@example.com");

        // Setup test card
        testCard = new Card();
        testCard.setCardNumber("4111111111111111");
        testCard.setExpirationDate("12/2025");
        testCard.setClient(testClient);

        // Setup test account
        testAccount = new Account();

        testAccount.setAccountNumber("ACCT123456789");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setClient(testClient);

        // Setup payment request
        validPaymentRequest = new PaymentRequestDTO();
        validPaymentRequest.setCardNumber("4111111111111111");
        validPaymentRequest.setExpirationDate("12/2025");
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setMerchantName("Test Merchant");

        // Setup transaction
        testTransaction = new TransactionEntity();
        testTransaction.setId(1L);
        testTransaction.setClientId(1L);
        testTransaction.setStatus("Initiated");
        testTransaction.setDebtorAccount("ACCT123456747");
        testTransaction.setClientAccountNumber("ACCT123456789");
        testTransaction.setTransferAmount(new BigDecimal("100.00"));

        // Set properties via reflection
        ReflectionTestUtils.setField(paymentService, "acqPort", 8444);
        ReflectionTestUtils.setField(paymentService, "clientTruststorePath", "client.truststore");
        ReflectionTestUtils.setField(paymentService, "clientTruststorePassword", "trustpass");
    }

    @Test
    void processPayment_ShouldReturnFalse_WhenAcqRejectsVerification() throws Exception {
        // Arrange
        String token = "123456";
        Long transactionId = 1L;

        when(cardRepository.findByCardNumberAndClientId("4111111111111111", 1L))
                .thenReturn(Optional.of(testCard));

        // Setup mock ACQ response with NACK
        String mockResponse = "Response from ACQ: NACK";
        SSLSocket mockSocket = mock(SSLSocket.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes());

        try (MockedStatic<SslUtils> sslUtilsMock = mockStatic(SslUtils.class)) {
            sslUtilsMock.when(() -> SslUtils.createSslClientSocket(anyInt(), anyString(), anyString()))
                    .thenReturn(mockSocket);

            when(mockSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSocket.getInputStream()).thenReturn(inputStream);

            // Act
            boolean result = paymentService.processPayment(token, validPaymentRequest, testClient, transactionId);

            // Assert
            assertFalse(result);
            verify(transactionService, never()).completePaymentTransaction(any(), any(), anyLong());
            verify(transactionService, never()).failTransaction(anyLong(), anyString());
        }
    }


    @Test
    void validateCardNumber_ShouldReturnTrue_ForValidCardNumber() {
        // Act & Assert
        assertTrue(paymentService.validateCardNumber("4111111111111111"));
        assertTrue(paymentService.validateCardNumber("5500 0000 0000 0004"));
        assertTrue(paymentService.validateCardNumber("340000000000009"));
    }

    @Test
    void validateCardNumber_ShouldReturnFalse_ForInvalidCardNumber() {
        // Act & Assert
        assertFalse(paymentService.validateCardNumber(null));
        assertFalse(paymentService.validateCardNumber(""));
        assertFalse(paymentService.validateCardNumber("1234")); // Too short
        assertFalse(paymentService.validateCardNumber("41111111111111112222222")); // Too long
        assertFalse(paymentService.validateCardNumber("4111111111111112")); // Fails Luhn check
        assertFalse(paymentService.validateCardNumber("abcdefghijklmnop")); // Non-numeric
    }

    @Test
    void isExpirationDateValid_ShouldReturnTrue_ForValidFutureDate() {
        // Get the next month
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        String nextMonthStr = nextMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        // Act & Assert
        assertTrue(paymentService.isExpirationDateValid(nextMonthStr));
        assertTrue(paymentService.isExpirationDateValid("12/2025"));
        assertTrue(paymentService.isExpirationDateValid("01/2030"));
    }

    @Test
    void isExpirationDateValid_ShouldReturnFalse_ForInvalidOrPastDate() {
        // Get previous month
        YearMonth prevMonth = YearMonth.now().minusMonths(1);
        String prevMonthStr = prevMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        // Act & Assert
        assertFalse(paymentService.isExpirationDateValid(null));
        assertFalse(paymentService.isExpirationDateValid(""));
        assertFalse(paymentService.isExpirationDateValid("13/2025")); // Invalid month
        assertFalse(paymentService.isExpirationDateValid("00/2025")); // Invalid month
        assertFalse(paymentService.isExpirationDateValid("12-2025")); // Wrong format
        assertFalse(paymentService.isExpirationDateValid("12/25")); // Short year format
        assertFalse(paymentService.isExpirationDateValid("12/2023")); // Past year
        assertFalse(paymentService.isExpirationDateValid(prevMonthStr)); // Past month
    }

    @Test
    void isAmountAcceptable_ShouldReturnTrueForValidAmount() {
        // Act & Assert
        assertTrue(paymentService.isAmountAcceptable(new BigDecimal("0.01")));
        assertTrue(paymentService.isAmountAcceptable(new BigDecimal("100.00")));
        assertTrue(paymentService.isAmountAcceptable(new BigDecimal("9999.99")));
    }

    @Test
    void isAmountAcceptable_ShouldReturnFalseForInvalidAmount() {
        // Act & Assert
        assertFalse(paymentService.isAmountAcceptable(null));
        assertFalse(paymentService.isAmountAcceptable(new BigDecimal("0.00")));
        assertFalse(paymentService.isAmountAcceptable(new BigDecimal("-10.00")));
        assertFalse(paymentService.isAmountAcceptable(new BigDecimal("10000.00"))); // Exceeds limit
    }

    @Test
    void getAccountBalance_ShouldReturnBalance_WhenAccountExists() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(1L))
                .thenReturn(Optional.of(testAccount));

        // Act
        BigDecimal result = paymentService.getAccountBalance(testClient);

        // Assert
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    void getAccountBalance_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> paymentService.getAccountBalance(testClient));

        assertEquals("Client has no associated account", exception.getMessage());
    }

    @Test
    void maskCardNumber_ShouldProperlyMaskCardNumber() {
        // Act & Assert
        assertEquals("************1111", paymentService.maskCardNumber("4111111111111111"));
        assertEquals("************5678", paymentService.maskCardNumber("1234567890125678"));
        assertEquals("****", paymentService.maskCardNumber("123")); // Short input
        assertEquals("****", paymentService.maskCardNumber(null)); // Null input
    }
}