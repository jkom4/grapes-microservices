package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.Merchant;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.MerchantRepository;
import grapes.microservices.paymentbackend.repositories.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TransactionService
 * Tests transaction creation, completion, failure handling, and lookup functionality
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    // Repository dependencies
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MerchantRepository merchantRepository;

    // Test objects and constants
    private Client testClient;
    private Account testClientAccount;
    private Account testGrapesAccount;
    private Merchant testMerchant;
    private PaymentRequestDTO testPaymentRequest;
    private TransactionEntity testTransaction;
    private static final String GRAPES_ACCOUNT_NUMBER = "BE15203672485394";

    @BeforeEach
    void setUp() {
        // Initialize test objects before each test
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFirstName("Test");
        testClient.setLastName("Client");

        testClientAccount = new Account();
        testClientAccount.setAccountNumber("BE12345678901234");
        testClientAccount.setBalance(new BigDecimal("1000.00"));
        testClientAccount.setStatus("Active");

        testGrapesAccount = new Account();
        testGrapesAccount.setAccountNumber(GRAPES_ACCOUNT_NUMBER);
        testGrapesAccount.setBalance(new BigDecimal("50000.00"));
        testGrapesAccount.setAccountType("Internal");
        testGrapesAccount.setStatus("Active");

        testMerchant = new Merchant();
        testMerchant.setMerchantName("TestMerchant");
        testMerchant.setBusinessSector("Retail");

        testPaymentRequest = new PaymentRequestDTO();
        testPaymentRequest.setAmount(new BigDecimal("50.00"));
        testPaymentRequest.setMerchantName("TestMerchant");

        // Create transaction with constructor matching service implementation
        testTransaction = new TransactionEntity(
                testClientAccount.getAccountNumber(), // debtorAccount
                "Test Bank", // debtorBankName
                testClient.getId(), // clientId
                testClientAccount.getAccountNumber(), // clientAccountNumber
                testPaymentRequest.getAmount(), // transferAmount
                testPaymentRequest.getMerchantName(), // creditorName (merchantName)
                testMerchant.getBusinessSector() // businessSector
        );
        testTransaction.setId(99L); // Simulate ID after initial save
        testTransaction.setStatus("Initiated"); // Expected initial state
    }

    /**
     * Tests creating a payment transaction when merchant exists
     * Expected: Transaction is created with correct details and Initiated status
     */
    @Test
    void createPaymentTransaction_Success_MerchantExists() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.of(testClientAccount));
        when(merchantRepository.findByMerchantName(testPaymentRequest.getMerchantName()))
                .thenReturn(Optional.of(testMerchant));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity tx = invocation.getArgument(0);
                    tx.setId(100L);
                    return tx;
                });

        // Act
        TransactionEntity createdTransaction = transactionService.createPaymentTransaction(testPaymentRequest, testClient);

        // Assert
        assertNotNull(createdTransaction);
        assertEquals(100L, createdTransaction.getId());
        assertEquals("Initiated", createdTransaction.getStatus());
        assertEquals(testClientAccount.getAccountNumber(), createdTransaction.getDebtorAccount());
        assertEquals(testClient.getId(), createdTransaction.getClientId());
        assertEquals(testPaymentRequest.getAmount(), createdTransaction.getTransferAmount());

        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
    }

    /**
     * Tests creating a payment transaction when merchant doesn't exist
     * Expected: Transaction is still created but with default/unknown merchant details
     */
    @Test
    void createPaymentTransaction_Success_MerchantNotFound() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.of(testClientAccount));
        when(merchantRepository.findByMerchantName("UnknownMerchant"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity tx = invocation.getArgument(0);
                    tx.setId(101L);
                    return tx;
                });
        testPaymentRequest.setMerchantName("UnknownMerchant");

        // Act
        TransactionEntity createdTransaction = transactionService.createPaymentTransaction(testPaymentRequest, testClient);

        // Assert
        assertNotNull(createdTransaction);
        assertEquals(101L, createdTransaction.getId());
        assertEquals("Initiated", createdTransaction.getStatus());

        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        verify(merchantRepository, times(1)).findByMerchantName("UnknownMerchant");
    }

    /**
     * Tests creating a payment transaction when merchant name is empty
     * Expected: Transaction is created with default merchant name "Grapes"
     */
    @Test
    void createPaymentTransaction_Success_MerchantNameNullOrEmpty() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.of(testClientAccount));
        when(merchantRepository.findByMerchantName("Grapes"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity tx = invocation.getArgument(0);
                    tx.setId(102L);
                    return tx;
                });
        testPaymentRequest.setMerchantName("");

        // Act
        TransactionEntity createdTransaction = transactionService.createPaymentTransaction(testPaymentRequest, testClient);

        // Assert
        assertNotNull(createdTransaction);
        assertEquals(102L, createdTransaction.getId());
        assertEquals("Initiated", createdTransaction.getStatus());

        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        verify(merchantRepository, times(1)).findByMerchantName("Grapes");
    }

    /**
     * Tests creating a payment transaction when client has no account
     * Expected: IllegalStateException is thrown
     */
    @Test
    void createPaymentTransaction_Failure_ClientAccountNotFound() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.createPaymentTransaction(testPaymentRequest, testClient));

        assertEquals("Client has no associated account to debit from.", exception.getMessage());
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    // --- Tests for completePaymentTransaction ---

    /**
     * Tests successful completion of a payment transaction
     * Expected: Transaction status is updated to "Completed" and account balances are updated
     */
    @Test
    void completePaymentTransaction_Success() {
        // Arrange
        BigDecimal amountToComplete = testTransaction.getTransferAmount();
        Long transactionId = testTransaction.getId();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        // Verify transaction initial state
        assertEquals(testClient.getId(), testTransaction.getClientId());
        assertEquals(0, testTransaction.getTransferAmount().compareTo(amountToComplete));
        assertEquals("Initiated", testTransaction.getStatus());

        when(accountRepository.findByAccountNumber(testClientAccount.getAccountNumber()))
                .thenReturn(Optional.of(testClientAccount));
        when(accountRepository.findByAccountNumber(GRAPES_ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testGrapesAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionEntity completedTransaction = transactionService.completePaymentTransaction(testClient, amountToComplete, transactionId);

        // Assert
        assertNotNull(completedTransaction);
        assertEquals("Completed", completedTransaction.getStatus());
        assertEquals("Validated", completedTransaction.getStatus3DS());
        assertNotNull(completedTransaction.getTransactionDateTime());
        assertEquals(0, new BigDecimal("950.00").compareTo(testClientAccount.getBalance()), "Client balance incorrect");
        assertEquals(0, new BigDecimal("50050.00").compareTo(testGrapesAccount.getBalance()), "Grapes balance incorrect");
        assertEquals(0, testClientAccount.getBalance().compareTo(completedTransaction.getDebtorAccountNewBalance()), "Debtor balance mismatch in TX");
        assertEquals(0, testGrapesAccount.getBalance().compareTo(completedTransaction.getCreditorAccountNewBalance()), "Creditor balance mismatch in TX");

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    /**
     * Tests transaction completion when transaction is not found
     * Expected: IllegalStateException is thrown
     */
    @Test
    void completePaymentTransaction_Failure_TransactionNotFound() {
        // Arrange
        Long transactionId = 12345L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.completePaymentTransaction(testClient, new BigDecimal("50.00"), transactionId));
        assertEquals("Transaction with ID " + transactionId + " not found for completion.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests transaction completion when transaction is already in completed state
     * Expected: IllegalStateException is thrown
     */
    @Test
    void completePaymentTransaction_Failure_WrongStatus() {
        // Arrange
        testTransaction.setStatus("Completed");
        Long transactionId = testTransaction.getId();
        BigDecimal amount = testTransaction.getTransferAmount();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.completePaymentTransaction(testClient, amount, transactionId));
        assertEquals("Transaction "+ transactionId + " is not in a completable state (Completed).", exception.getMessage());
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests transaction completion with a different client than the transaction owner
     * Expected: SecurityException is thrown
     */
    @Test
    void completePaymentTransaction_Failure_ClientMismatch() {
        // Arrange
        Long transactionId = testTransaction.getId();
        BigDecimal amount = testTransaction.getTransferAmount();
        Client anotherClient = new Client();
        anotherClient.setId(2L);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> transactionService.completePaymentTransaction(anotherClient, amount, transactionId));
        assertEquals("Client mismatch for the transaction completion.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests transaction completion with a different amount than the original transaction
     * Expected: IllegalStateException is thrown and transaction is marked as failed
     */
    @Test
    void completePaymentTransaction_Failure_AmountMismatch() {
        // Arrange
        Long transactionId = testTransaction.getId();
        BigDecimal wrongAmount = new BigDecimal("50.01");

        // Setup repository to return normal transaction
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create spy on transaction to verify markAsFailed is called
        TransactionEntity transactionSpy = spy(testTransaction);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionSpy));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.completePaymentTransaction(testClient, wrongAmount, transactionId));
        assertEquals("Amount mismatch during transaction completion.", exception.getMessage());

        // Verify failure handling
        verify(transactionSpy).markAsFailed("Amount Mismatch");
        assertEquals("Failed", transactionSpy.getStatus());
        verify(transactionRepository, times(1)).save(transactionSpy);
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests transaction completion when client account can't be found in database
     * Expected: IllegalStateException is thrown and transaction is marked as failed
     */
    @Test
    void completePaymentTransaction_Failure_ClientAccountNotFoundInDB() {
        // Arrange
        Long transactionId = testTransaction.getId();
        BigDecimal amount = testTransaction.getTransferAmount();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(accountRepository.findByAccountNumber(testTransaction.getClientAccountNumber()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create spy to verify failure handling
        TransactionEntity transactionSpy = spy(testTransaction);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionSpy));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.completePaymentTransaction(testClient, amount, transactionId));
        assertEquals("Client account associated with the transaction not found.", exception.getMessage());

        // Verify failure handling
        verify(transactionSpy).markAsFailed("Client Account Not Found");
        assertEquals("Failed", transactionSpy.getStatus());
        verify(transactionRepository, times(1)).save(transactionSpy);
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests transaction completion when client account has insufficient balance
     * Expected: IllegalStateException is thrown and transaction is marked as failed
     */
    @Test
    void completePaymentTransaction_Failure_InsufficientBalance() {
        // Arrange
        Long transactionId = testTransaction.getId();
        BigDecimal amount = testTransaction.getTransferAmount();
        testClientAccount.setBalance(new BigDecimal("49.99"));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(accountRepository.findByAccountNumber(testClientAccount.getAccountNumber()))
                .thenReturn(Optional.of(testClientAccount));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create spy to verify failure handling
        TransactionEntity transactionSpy = spy(testTransaction);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionSpy));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionService.completePaymentTransaction(testClient, amount, transactionId));
        assertEquals("Insufficient account balance to complete the payment.", exception.getMessage());

        // Verify failure handling
        verify(transactionSpy).markAsFailed("Insufficient Balance");
        assertEquals("Failed", transactionSpy.getStatus());
        verify(transactionRepository, times(1)).save(transactionSpy);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- Tests for failTransaction ---

    /**
     * Tests failing a transaction that doesn't exist
     * Expected: Returns null without updates
     */
    @Test
    void failTransaction_TransactionNotFound() {
        // Arrange
        Long transactionId = 12345L;
        String reason = "Doesn't matter";
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act
        TransactionEntity result = transactionService.failTransaction(transactionId, reason);

        // Assert
        assertNull(result);
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, never()).save(any());
    }

    /**
     * Tests failing a transaction that is already in completed state
     * Expected: Returns transaction without changes
     */
    @Test
    void failTransaction_AlreadyCompleted() {
        // Arrange
        Long transactionId = testTransaction.getId();
        String reason = "Trying to fail completed";
        testTransaction.setStatus("Completed");
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // Act
        TransactionEntity result = transactionService.failTransaction(transactionId, reason);

        // Assert
        assertNotNull(result);
        assertEquals(testTransaction, result);
        assertEquals("Completed", result.getStatus());

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, never()).save(any());
    }

    /**
     * Tests failing a transaction that is already in failed state
     * Expected: Returns transaction without changes
     */
    @Test
    void failTransaction_AlreadyFailed() {
        // Arrange
        Long transactionId = testTransaction.getId();
        String newReason = "Trying to fail again";
        testTransaction.setStatus("Failed");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // Act
        TransactionEntity result = transactionService.failTransaction(transactionId, newReason);

        // Assert
        assertNotNull(result);
        assertEquals(testTransaction, result);
        assertEquals("Failed", result.getStatus());

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, never()).save(any());
    }

    // --- Tests for findTransactionById ---

    /**
     * Tests finding a transaction by ID when it exists
     * Expected: Returns an Optional containing the transaction
     */
    @Test
    void findTransactionById_Found() {
        // Arrange
        Long transactionId = testTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // Act
        Optional<TransactionEntity> foundTransactionOpt = transactionService.findTransactionById(transactionId);

        // Assert
        assertTrue(foundTransactionOpt.isPresent());
        assertEquals(testTransaction, foundTransactionOpt.get());
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    /**
     * Tests finding a transaction by ID when it doesn't exist
     * Expected: Returns an empty Optional
     */
    @Test
    void findTransactionById_NotFound() {
        // Arrange
        Long transactionId = 12345L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act
        Optional<TransactionEntity> foundTransactionOpt = transactionService.findTransactionById(transactionId);

        // Assert
        assertTrue(foundTransactionOpt.isEmpty());
        verify(transactionRepository, times(1)).findById(transactionId);
    }
}