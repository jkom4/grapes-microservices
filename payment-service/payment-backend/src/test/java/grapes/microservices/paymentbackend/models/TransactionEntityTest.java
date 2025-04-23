package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class TransactionEntityTest {

    @Test
    void noArgsConstructor_createsTransactionEntity() {
        // Test the no-argument constructor
        TransactionEntity transaction = new TransactionEntity();
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getStatus());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        // Test the all-argument constructor
        Long id = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal debtorBalance = new BigDecimal("900.00");
        BigDecimal creditorBalance = new BigDecimal("1100.00");

        TransactionEntity transaction = new TransactionEntity(
                id, "DEBTOR_ACC", "CREDITOR_ACC", "DEBTOR_BANK", "CREDITOR_BANK",
                1L, "Payment", "CLIENT_ACC", now, amount, "Merchant A", "Retail",
                "OTP", debtorBalance, creditorBalance, "Completed"
        );

        // Assert that all fields were set correctly
        assertEquals(id, transaction.getId());
        assertEquals("DEBTOR_ACC", transaction.getDebtorAccount());
        assertEquals("CREDITOR_ACC", transaction.getCreditorAccount());
        assertEquals("DEBTOR_BANK", transaction.getDebtorBank());
        assertEquals("CREDITOR_BANK", transaction.getCreditorBank());
        assertEquals(1L, transaction.getClientId());
        assertEquals("Payment", transaction.getTransactionType());
        assertEquals("CLIENT_ACC", transaction.getClientAccountNumber());
        assertEquals(now, transaction.getTransactionDateTime());
        assertEquals(0, amount.compareTo(transaction.getTransferAmount()));
        assertEquals("Merchant A", transaction.getMerchantName());
        assertEquals("Retail", transaction.getMerchantBusinessSector());
        assertEquals("OTP", transaction.getAuthenticationType3DS());
        assertEquals(0, debtorBalance.compareTo(transaction.getDebtorAccountNewBalance()));
        assertEquals(0, creditorBalance.compareTo(transaction.getCreditorAccountNewBalance()));
        assertEquals("Completed", transaction.getStatus());
    }

    @Test
    void customConstructor_initializesDefaultsAndGeneratesId() {
        // Test the constructor used for initiating a new payment transaction
        String debtorAcc = "BE_DEBTOR_123";
        String debtorBank = "Debtor Bank";
        Long clientId = 10L;
        BigDecimal amount = new BigDecimal("49.99");
        String merchant = "Test Merchant";
        String sector = "Test Sector";

        TransactionEntity transaction = new TransactionEntity(
                debtorAcc, debtorBank, clientId, debtorAcc, // Passing debtorAcc also as clientAccountNumber
                amount, merchant, sector);

        // Verify generated/default fields
        assertNotNull(transaction.getId(), "Transaction ID should be generated");
        assertNotNull(transaction.getTransactionDateTime(), "Transaction date/time should be set");
        assertEquals("Payment", transaction.getTransactionType());
        assertEquals("OTP", transaction.getAuthenticationType3DS());
        assertEquals("Initiated", transaction.getStatus());
        assertEquals("BE15203672485394", transaction.getCreditorAccount()); // Check default creditor
        assertEquals("Grapes's bank", transaction.getCreditorBank());      // Check default creditor bank

        // Verify passed-in fields
        assertEquals(debtorAcc, transaction.getDebtorAccount());
        assertEquals(debtorBank, transaction.getDebtorBank());
        assertEquals(clientId, transaction.getClientId());
        assertEquals(debtorAcc, transaction.getClientAccountNumber());
        assertEquals(0, amount.compareTo(transaction.getTransferAmount()));
        assertEquals(merchant, transaction.getMerchantName());
        assertEquals(sector, transaction.getMerchantBusinessSector());

        // Verify fields that shouldn't be set yet
        assertNull(transaction.getDebtorAccountNewBalance());
        assertNull(transaction.getCreditorAccountNewBalance());
    }

    @Test
    void markAsCompleted_updatesStatusAndBalance() {
        // Test the state change when marking a transaction as completed
        TransactionEntity transaction = new TransactionEntity(
                "ACC1", "Bank1", 1L, "ACC1", new BigDecimal("10.0"), "M1", "S1");
        // Initial state check (set by constructor)
        assertEquals("Initiated", transaction.getStatus());
        assertNull(transaction.getDebtorAccountNewBalance());

        BigDecimal newDebtorBalance = new BigDecimal("990.00");
        transaction.markAsCompleted(newDebtorBalance);

        // Assert updated state
        assertEquals("Completed", transaction.getStatus());
        assertEquals(0, newDebtorBalance.compareTo(transaction.getDebtorAccountNewBalance()));
    }

    @Test
    void markAsFailed_updatesStatus() {
        // Test the state change when marking a transaction as failed
        TransactionEntity transaction = new TransactionEntity(
                "ACC2", "Bank2", 2L, "ACC2", new BigDecimal("20.0"), "M2", "S2");
        // Initial state check
        assertEquals("Initiated", transaction.getStatus());

        transaction.markAsFailed("Insufficient funds");

        // Assert updated state
        assertEquals("Failed", transaction.getStatus());
        // Balances should ideally remain null or unchanged upon failure in this logic
        assertNull(transaction.getDebtorAccountNewBalance());
    }
}