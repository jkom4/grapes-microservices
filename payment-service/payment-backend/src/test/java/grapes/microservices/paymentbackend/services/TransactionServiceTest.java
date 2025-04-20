package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Client testClient;
    private Account testAccount;
    private PaymentRequestDTO testPaymentRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testClient = new Client();
        testClient.setId(1L);

        testAccount = new Account();
        testAccount.setAccountNumber("1234567890");
        testAccount.setBalance(BigDecimal.valueOf(1000));
        testAccount.setClient(testClient);

        testPaymentRequest = new PaymentRequestDTO();
        testPaymentRequest.setAmount(BigDecimal.valueOf(100));
        testPaymentRequest.setMerchantName("TestMerchant");
    }

    @Test
    void createPaymentTransaction_Success() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.of(testAccount));

        when(merchantRepository.findByMerchantName(anyString()))
                .thenReturn(Optional.of(new Merchant()));

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity savedTransaction = invocation.getArgument(0);
                    savedTransaction.setId(1L);
                    return savedTransaction;
                });

        // Act
        TransactionEntity transaction = transactionService.createPaymentTransaction(testPaymentRequest, testClient);

        // Assert
        assertNotNull(transaction);
        assertEquals(testPaymentRequest.getAmount(), transaction.getTransferAmount());
        assertEquals(testClient.getId(), transaction.getClientId());
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void failTransaction_Success() {
        // Arrange
        TransactionEntity existingTransaction = new TransactionEntity();
        existingTransaction.setId(1L);
        existingTransaction.setStatus("Initiated");

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionEntity failedTransaction = transactionService.failTransaction(1L, "Test Failure");

        // Assert
        assertNotNull(failedTransaction);
        assertEquals("Failed", failedTransaction.getStatus());
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void findTransactionById_Success() {
        // Arrange
        TransactionEntity existingTransaction = new TransactionEntity();
        existingTransaction.setId(1L);

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));

        // Act
        Optional<TransactionEntity> foundTransaction = transactionService.findTransactionById(1L);

        // Assert
        assertTrue(foundTransaction.isPresent());
        assertEquals(1L, foundTransaction.get().getId());
    }

    @Test
    void createPaymentTransaction_NoAccount_ShouldThrowException() {
        // Arrange
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(testClient.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                transactionService.createPaymentTransaction(testPaymentRequest, testClient)
        );
    }
}