package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.CompletePaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private CardService cardService;

    @Mock
    private ClientService clientService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private HttpSession session;

    @Mock
    private Cache pendingPaymentsCache;

    @Mock
    private Cache.ValueWrapper cacheValueWrapper;

    @InjectMocks
    private PaymentProcessingService paymentProcessingService;

    private Client testClient;
    private Card testCard;
    private PaymentRequestDTO validPaymentRequest;
    private CompletePaymentRequestDTO validCompleteRequest;
    private TransactionEntity testTransaction;

    @BeforeEach
    void setUp() {
        // Set up test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setEmail("test@example.com");

        // Set up test card
        testCard = new Card();
        testCard.setCardNumber("4111111111111111");
        testCard.setExpirationDate("12/25");
        testCard.setClient(testClient);

        // Set up valid payment request
        validPaymentRequest = new PaymentRequestDTO();
        validPaymentRequest.setCardNumber("4111111111111111");
        validPaymentRequest.setExpirationDate("12/25");
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setMerchantName("Test Merchant");

        // Set up valid complete payment request
        validCompleteRequest = new CompletePaymentRequestDTO();
        validCompleteRequest.setTransactionId(1L);
        validCompleteRequest.setPaymentToken("123456");

        // Set up test transaction
        testTransaction = new TransactionEntity();
        testTransaction.setId(1L);
        testTransaction.setClientId(1L);
        testTransaction.setTransferAmount(new BigDecimal("100.00"));
        testTransaction.setMerchantName("Test Merchant");
        testTransaction.setStatus("Initiated");
    }

    @Test
    void initiatePayment_ShouldReturnUnauthorized_WhenClientNotAuthenticated() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Client session not found or expired. Please log in again.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnNotFound_WhenClientNotInDatabase() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Client associated with session not found. Please log in again.", responseBody.get("message"));
        verify(session).invalidate();
    }

    @Test
    void initiatePayment_ShouldUseMergedPaymentRequest_WhenSessionHasInitialData() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));

        BigDecimal sessionAmount = new BigDecimal("200.00");
        String sessionMerchant = "Session Merchant";
        when(session.getAttribute("initialPaymentAmount")).thenReturn(sessionAmount);
        when(session.getAttribute("initialMerchantName")).thenReturn(sessionMerchant);

        // Mock validation methods
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);
        List<Card> cards = Collections.singletonList(testCard);
        when(clientService.getClientCards(1L)).thenReturn(cards);
        when(paymentService.getAccountBalance(testClient)).thenReturn(new BigDecimal("1000.00"));
        when(paymentService.isAmountAcceptable(any(BigDecimal.class))).thenReturn(true);

        // Mock transaction creation
        when(transactionService.createPaymentTransaction(any(PaymentRequestDTO.class), any(Client.class)))
                .thenReturn(testTransaction);

        // Mock cache behavior
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);

        // Mock card verification
        when(cardService.initiateCardVerification(any(PaymentRequestDTO.class), any(Client.class), anyString()))
                .thenReturn("123456");

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Verification required. Check your phone for the OTP code.", responseBody.get("message"));
        assertEquals(1L, responseBody.get("transactionId"));

        // Verify session attributes were removed
        verify(session).removeAttribute("initialPaymentAmount");
        verify(session).removeAttribute("initialMerchantName");
        verify(session).removeAttribute("initialPaymentId");

        // Verify the request was modified with session data
        verify(transactionService).createPaymentTransaction(argThat(request ->
                        request.getAmount().equals(sessionAmount) &&
                                request.getMerchantName().equals(sessionMerchant)),
                eq(testClient));
    }

    @Test
    void initiatePayment_ShouldReturnBadRequest_WhenCardValidationFails() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(paymentService.validateCardNumber("4111111111111111")).thenReturn(false);

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Invalid card number format.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnBadRequest_WhenExpirationDateInvalid() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(false);

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Card has expired or expiration date format is invalid.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnBadRequest_WhenCardNotAssociatedWithAccount() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);

        // Return empty list of cards (no cards associated with account)
        when(clientService.getClientCards(1L)).thenReturn(Collections.emptyList());
        when(paymentService.maskCardNumber(anyString())).thenReturn("411111******1111");

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("This card is not associated with your account.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnBadRequest_WhenExpirationDateMismatch() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);

        // Create card with different expiration date
        Card cardWithDifferentExp = new Card();
        cardWithDifferentExp.setCardNumber("4111111111111111");
        cardWithDifferentExp.setExpirationDate("11/26"); // Different from request's 12/25
        cardWithDifferentExp.setClient(testClient);

        when(clientService.getClientCards(1L)).thenReturn(Collections.singletonList(cardWithDifferentExp));

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Card expiration date doesn't match our records.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnBadRequest_WhenInsufficientFunds() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);

        // Return a card that matches request
        List<Card> cards = Collections.singletonList(testCard);
        when(clientService.getClientCards(1L)).thenReturn(cards);

        // Simulate insufficient funds
        when(paymentService.getAccountBalance(testClient)).thenReturn(new BigDecimal("50.00")); // Less than 100.00

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Insufficient funds in your account to complete this transaction.", responseBody.get("message"));
    }

    @Test
    void initiatePayment_ShouldReturnSuccess_WhenEverythingValid() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));

        // Mock validation methods
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);
        List<Card> cards = Collections.singletonList(testCard);
        when(clientService.getClientCards(1L)).thenReturn(cards);
        when(paymentService.getAccountBalance(testClient)).thenReturn(new BigDecimal("1000.00"));
        when(paymentService.isAmountAcceptable(any(BigDecimal.class))).thenReturn(true);

        // Mock transaction creation
        when(transactionService.createPaymentTransaction(any(PaymentRequestDTO.class), any(Client.class)))
                .thenReturn(testTransaction);

        // Mock cache behavior
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);

        // Mock card verification
        when(cardService.initiateCardVerification(any(PaymentRequestDTO.class), any(Client.class), anyString()))
                .thenReturn("123456");

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Verification required. Check your phone for the OTP code.", responseBody.get("message"));
        assertEquals(1L, responseBody.get("transactionId"));

        // Verify cache operations
        verify(pendingPaymentsCache).put(1L, validPaymentRequest);
    }

    @Test
    void initiatePayment_ShouldReturnError_WhenCardVerificationFails() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));

        // Mock validation methods
        when(paymentService.validateCardNumber(anyString())).thenReturn(true);
        when(paymentService.isExpirationDateValid(anyString())).thenReturn(true);
        List<Card> cards = Collections.singletonList(testCard);
        when(clientService.getClientCards(1L)).thenReturn(cards);
        when(paymentService.getAccountBalance(testClient)).thenReturn(new BigDecimal("1000.00"));
        when(paymentService.isAmountAcceptable(any(BigDecimal.class))).thenReturn(true);

        // Mock transaction creation
        when(transactionService.createPaymentTransaction(any(PaymentRequestDTO.class), any(Client.class)))
                .thenReturn(testTransaction);

        // Mock cache behavior
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);

        // Mock card verification failure
        when(cardService.initiateCardVerification(any(PaymentRequestDTO.class), any(Client.class), anyString()))
                .thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.initiatePayment(validPaymentRequest, session);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Failed to initiate 3D Secure verification with the bank. Please try again later.", responseBody.get("message"));

        // Verify transaction was marked as failed and cache entry was removed
        verify(transactionService).failTransaction(1L, "ACS Initiation Failed");
        verify(pendingPaymentsCache).evictIfPresent(1L);
    }

    @Test
    void completePayment_ShouldReturnUnauthorized_WhenClientNotAuthenticated() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.completePayment(validCompleteRequest, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Client session not found or expired. Please log in again.", responseBody.get("message"));
    }

    @Test
    void completePayment_ShouldReturnNotFound_WhenClientNotInDatabase() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = paymentProcessingService.completePayment(validCompleteRequest, session);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Client associated with session not found.", responseBody.get("message"));
    }

    @Test
    void completePayment_ShouldReturnBadRequest_WhenTransactionNotInCache() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(null);

        // Set up transaction as completed
        testTransaction.setStatus("Completed");
        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(testTransaction));

        // Act
        ResponseEntity<?> response = paymentProcessingService.completePayment(validCompleteRequest, session);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("This payment has already been processed successfully.", responseBody.get("message"));
    }

    @Test
    void completePayment_ShouldReturnSuccess_WhenPaymentSuccessful() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(validPaymentRequest);

        // Mock successful payment
        when(paymentService.processPayment(
                eq("123456"),
                eq(validPaymentRequest),
                eq(testClient),
                eq(1L),
                eq(1L))).thenReturn(true);

        // Act
        ResponseEntity<?> response = paymentProcessingService.completePayment(validCompleteRequest, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Payment processed successfully", responseBody.get("message"));
        assertEquals(1L, responseBody.get("transactionId"));

        // Verify cache was cleaned
        verify(pendingPaymentsCache).evict(1L);
    }

    @Test
    void completePayment_ShouldReturnBadRequest_WhenVerificationFails() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(validPaymentRequest);

        // Mock failed payment
        when(paymentService.processPayment(
                eq("123456"),
                eq(validPaymentRequest),
                eq(testClient),
                eq(1L),
                eq(1L))).thenReturn(false);

        // Act
        ResponseEntity<?> response = paymentProcessingService.completePayment(validCompleteRequest, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Verification failed. The code entered is incorrect or has expired.", responseBody.get("message"));
    }

    @Test
    void getPendingPaymentDetails_ShouldReturnUnauthorized_WhenClientNotAuthenticated() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.getPendingPaymentDetails(1L, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Client session not found or expired.", responseBody.get("message"));
    }

    @Test
    void getPendingPaymentDetails_ShouldReturnNotFound_WhenNoPaymentInCache() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.getPendingPaymentDetails(1L, session);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("No pending payment found or it has expired.", responseBody.get("message"));
    }

    @Test
    void getPendingPaymentDetails_ShouldReturnForbidden_WhenTransactionNotOwnedByClient() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(validPaymentRequest);

        // Create transaction with different client ID
        TransactionEntity differentClientTransaction = new TransactionEntity();
        differentClientTransaction.setId(1L);
        differentClientTransaction.setClientId(2L); // Different from session client ID
        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(differentClientTransaction));

        // Act
        ResponseEntity<?> response = paymentProcessingService.getPendingPaymentDetails(1L, session);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Access denied to payment details.", responseBody.get("message"));
    }

    @Test
    void getPendingPaymentDetails_ShouldReturnGone_WhenTransactionNoLongerPending() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(validPaymentRequest);

        // Create completed transaction
        TransactionEntity completedTransaction = new TransactionEntity();
        completedTransaction.setId(1L);
        completedTransaction.setClientId(1L);
        completedTransaction.setStatus("Completed"); // Not "Initiated"
        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(completedTransaction));

        // Act
        ResponseEntity<?> response = paymentProcessingService.getPendingPaymentDetails(1L, session);

        // Assert
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("This payment is no longer pending verification.", responseBody.get("message"));
    }

    @Test
    void getPendingPaymentDetails_ShouldReturnSuccess_WhenDetailsAvailable() {
        // Arrange
        when(session.getAttribute("clientId")).thenReturn(1L);
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);
        when(pendingPaymentsCache.get(1L)).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(validPaymentRequest);

        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(testTransaction));
        when(paymentService.maskCardNumber("4111111111111111")).thenReturn("411111******1111");

        // Act
        ResponseEntity<?> response = paymentProcessingService.getPendingPaymentDetails(1L, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Test Merchant", responseBody.get("merchantName"));
        assertEquals("EUR 100,00", responseBody.get("amount"));
        assertEquals("411111******1111", responseBody.get("maskedCardNumber"));
    }

    @Test
    void getSessionDetails_ShouldReturnNotFound_WhenNoPaymentDetailsInSession() {
        // Arrange
        when(session.getAttribute("initialPaymentAmount")).thenReturn(null);

        // Act
        ResponseEntity<?> response = paymentProcessingService.getSessionDetails(session);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("No payment details found in session", responseBody.get("message"));
    }

    @Test
    void getSessionDetails_ShouldReturnSuccess_WhenDetailsFound() {
        // Arrange
        when(session.getAttribute("initialPaymentAmount")).thenReturn(new BigDecimal("100.00"));
        when(session.getAttribute("initialMerchantName")).thenReturn("Test Merchant");

        // Act
        ResponseEntity<?> response = paymentProcessingService.getSessionDetails(session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals(new BigDecimal("100.00"), responseBody.get("amount"));
        assertEquals("Test Merchant", responseBody.get("merchantName"));
    }

    @Test
    void handleInitiationError_ShouldReturnInternalServerError() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");

        // Act
        ResponseEntity<?> response = paymentProcessingService.handleInitiationError(testException);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("An unexpected error occurred during payment initiation.", responseBody.get("message"));
    }

    @Test
    void handleCompletionError_ShouldClearCacheAndReturnInternalServerError() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        when(cacheManager.getCache("pendingPayments")).thenReturn(pendingPaymentsCache);

        // Act
        ResponseEntity<?> response = paymentProcessingService.handleCompletionError(testException, 1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("An internal error occurred during payment completion.", responseBody.get("message"));
        verify(pendingPaymentsCache).evictIfPresent(1L);
    }

    @Test
    void handlePendingDetailsError_ShouldReturnInternalServerError() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");

        // Act
        ResponseEntity<?> response = paymentProcessingService.handlePendingDetailsError(testException);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Error retrieving payment details.", responseBody.get("message"));
    }
}