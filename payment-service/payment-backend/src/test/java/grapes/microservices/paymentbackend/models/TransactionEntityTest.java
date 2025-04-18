package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TransactionEntity model
 * Tests constructors, state transition methods, getters, setters, and standard object methods
 */
public class TransactionEntityTest {

    private TransactionEntity transaction;
    private final String debtorAccount = "BE98765432109876";
    private final String debtorBank = "Test Bank";
    private final Long clientId = 1234L;
    private final String clientAccountNumber = "BE98765432109876";
    private final BigDecimal transferAmount = new BigDecimal("100.00");
    private final String merchantName = "Test Merchant";
    private final String merchantBusinessSector = "Retail";

    @BeforeEach
    public void setUp() {
        // Create a transaction using the parameterized constructor with test values
        transaction = new TransactionEntity(
                debtorAccount,
                debtorBank,
                clientId,
                clientAccountNumber,
                transferAmount,
                merchantName,
                merchantBusinessSector
        );
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty TransactionEntity with all fields null
     */
    @Test
    public void testNoArgsConstructor() {
        TransactionEntity emptyTransaction = new TransactionEntity();

        assertNotNull(emptyTransaction);
        assertNull(emptyTransaction.getId());
        assertNull(emptyTransaction.getDebtorAccount());
        assertNull(emptyTransaction.getCreditorAccount());
        assertNull(emptyTransaction.getDebtorBank());
        assertNull(emptyTransaction.getCreditorBank());
        assertNull(emptyTransaction.getClientId());
        assertNull(emptyTransaction.getTransactionType());
        assertNull(emptyTransaction.getClientAccountNumber());
        assertNull(emptyTransaction.getTransactionDateTime());
        assertNull(emptyTransaction.getTransferAmount());
        assertNull(emptyTransaction.getMerchantName());
        assertNull(emptyTransaction.getMerchantBusinessSector());
        assertNull(emptyTransaction.getAuthenticationType3DS());
        assertNull(emptyTransaction.getStatus3DS());
        assertNull(emptyTransaction.getDebtorAccountNewBalance());
        assertNull(emptyTransaction.getCreditorAccountNewBalance());
        assertNull(emptyTransaction.getStatus());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates a TransactionEntity with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDateTime testDateTime = LocalDateTime.now().minusHours(1);
        TransactionEntity fullTransaction = new TransactionEntity(
                999L,
                "BE11111111111111",
                "BE22222222222222",
                "Debtor Bank",
                "Creditor Bank",
                5678L,
                "Transfer",
                "BE11111111111111",
                testDateTime,
                new BigDecimal("250.00"),
                "Other Merchant",
                "Services",
                "Biometric",
                "Validated",
                new BigDecimal("750.00"),
                new BigDecimal("1250.00"),
                "Completed"
        );

        assertEquals(999L, fullTransaction.getId());
        assertEquals("BE11111111111111", fullTransaction.getDebtorAccount());
        assertEquals("BE22222222222222", fullTransaction.getCreditorAccount());
        assertEquals("Debtor Bank", fullTransaction.getDebtorBank());
        assertEquals("Creditor Bank", fullTransaction.getCreditorBank());
        assertEquals(5678L, fullTransaction.getClientId());
        assertEquals("Transfer", fullTransaction.getTransactionType());
        assertEquals("BE11111111111111", fullTransaction.getClientAccountNumber());
        assertEquals(testDateTime, fullTransaction.getTransactionDateTime());
        assertEquals(new BigDecimal("250.00"), fullTransaction.getTransferAmount());
        assertEquals("Other Merchant", fullTransaction.getMerchantName());
        assertEquals("Services", fullTransaction.getMerchantBusinessSector());
        assertEquals("Biometric", fullTransaction.getAuthenticationType3DS());
        assertEquals("Validated", fullTransaction.getStatus3DS());
        assertEquals(new BigDecimal("750.00"), fullTransaction.getDebtorAccountNewBalance());
        assertEquals(new BigDecimal("1250.00"), fullTransaction.getCreditorAccountNewBalance());
        assertEquals("Completed", fullTransaction.getStatus());
    }

    /**
     * Tests the parameterized constructor (basic payment creation)
     * Expected: Sets provided fields and initializes defaults including Grapes system account
     */
    @Test
    public void testParameterizedConstructor() {
        // Verify ID was auto-generated
        assertNotNull(transaction.getId());

        // Verify fields set directly from constructor parameters
        assertEquals(clientAccountNumber, transaction.getDebtorAccount());
        assertEquals(debtorBank, transaction.getDebtorBank());
        assertEquals(clientId, transaction.getClientId());
        assertEquals(clientAccountNumber, transaction.getClientAccountNumber());
        assertEquals(transferAmount, transaction.getTransferAmount());
        assertEquals(merchantName, transaction.getMerchantName());
        assertEquals(merchantBusinessSector, transaction.getMerchantBusinessSector());

        // Verify default values were set
        assertEquals("Payment", transaction.getTransactionType());
        assertEquals("OTP", transaction.getAuthenticationType3DS());
        assertEquals("Pending", transaction.getStatus3DS());
        assertEquals("Initiated", transaction.getStatus());

        // Verify Grapes system account details were set as creditor
        assertEquals("BE15203672485394", transaction.getCreditorAccount());
        assertEquals("Bank of Grapes", transaction.getCreditorBank());

        // Verify transaction timestamp is recent (within last minute)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime transactionTime = transaction.getTransactionDateTime();
        long secondsBetween = ChronoUnit.SECONDS.between(transactionTime, now);
        assertTrue(secondsBetween < 60, "Transaction time should be recent");

        // Verify balance fields are initially null (set during completion)
        assertNull(transaction.getDebtorAccountNewBalance());
        assertNull(transaction.getCreditorAccountNewBalance());
    }

    /**
     * Tests the markAsCompleted method
     * Expected: Updates status fields and sets new balance
     */
    @Test
    public void testMarkAsCompleted() {
        BigDecimal newBalance = new BigDecimal("900.00");
        transaction.markAsCompleted(newBalance);

        assertEquals("Validated", transaction.getStatus3DS());
        assertEquals("Completed", transaction.getStatus());
        assertEquals(newBalance, transaction.getDebtorAccountNewBalance());
    }

    /**
     * Tests the markAsFailed method
     * Expected: Updates status fields to Failed
     */
    @Test
    public void testMarkAsFailed() {
        transaction.markAsFailed("Insufficient funds");

        assertEquals("Failed", transaction.getStatus3DS());
        assertEquals("Failed", transaction.getStatus());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        LocalDateTime newDateTime = LocalDateTime.now().minusDays(1);

        // Update all fields with new values
        transaction.setId(5555L);
        transaction.setDebtorAccount("BE44444444444444");
        transaction.setCreditorAccount("BE55555555555555");
        transaction.setDebtorBank("Updated Debtor Bank");
        transaction.setCreditorBank("Updated Creditor Bank");
        transaction.setClientId(9999L);
        transaction.setTransactionType("Refund");
        transaction.setClientAccountNumber("BE44444444444444");
        transaction.setTransactionDateTime(newDateTime);
        transaction.setTransferAmount(new BigDecimal("200.00"));
        transaction.setMerchantName("Updated Merchant");
        transaction.setMerchantBusinessSector("Food");
        transaction.setAuthenticationType3DS("SMS");
        transaction.setStatus3DS("Validated");
        transaction.setDebtorAccountNewBalance(new BigDecimal("800.00"));
        transaction.setCreditorAccountNewBalance(new BigDecimal("1200.00"));
        transaction.setStatus("Completed");

        // Verify all fields were updated correctly
        assertEquals(5555L, transaction.getId());
        assertEquals("BE44444444444444", transaction.getDebtorAccount());
        assertEquals("BE55555555555555", transaction.getCreditorAccount());
        assertEquals("Updated Debtor Bank", transaction.getDebtorBank());
        assertEquals("Updated Creditor Bank", transaction.getCreditorBank());
        assertEquals(9999L, transaction.getClientId());
        assertEquals("Refund", transaction.getTransactionType());
        assertEquals("BE44444444444444", transaction.getClientAccountNumber());
        assertEquals(newDateTime, transaction.getTransactionDateTime());
        assertEquals(new BigDecimal("200.00"), transaction.getTransferAmount());
        assertEquals("Updated Merchant", transaction.getMerchantName());
        assertEquals("Food", transaction.getMerchantBusinessSector());
        assertEquals("SMS", transaction.getAuthenticationType3DS());
        assertEquals("Validated", transaction.getStatus3DS());
        assertEquals(new BigDecimal("800.00"), transaction.getDebtorAccountNewBalance());
        assertEquals(new BigDecimal("1200.00"), transaction.getCreditorAccountNewBalance());
        assertEquals("Completed", transaction.getStatus());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create a complete copy with the same field values
        TransactionEntity sameTransaction = new TransactionEntity();
        sameTransaction.setId(transaction.getId());
        sameTransaction.setDebtorAccount(transaction.getDebtorAccount());
        sameTransaction.setCreditorAccount(transaction.getCreditorAccount());
        sameTransaction.setDebtorBank(transaction.getDebtorBank());
        sameTransaction.setCreditorBank(transaction.getCreditorBank());
        sameTransaction.setClientId(transaction.getClientId());
        sameTransaction.setTransactionType(transaction.getTransactionType());
        sameTransaction.setClientAccountNumber(transaction.getClientAccountNumber());
        sameTransaction.setTransactionDateTime(transaction.getTransactionDateTime());
        sameTransaction.setTransferAmount(transaction.getTransferAmount());
        sameTransaction.setMerchantName(transaction.getMerchantName());
        sameTransaction.setMerchantBusinessSector(transaction.getMerchantBusinessSector());
        sameTransaction.setAuthenticationType3DS(transaction.getAuthenticationType3DS());
        sameTransaction.setStatus3DS(transaction.getStatus3DS());
        sameTransaction.setDebtorAccountNewBalance(transaction.getDebtorAccountNewBalance());
        sameTransaction.setCreditorAccountNewBalance(transaction.getCreditorAccountNewBalance());
        sameTransaction.setStatus(transaction.getStatus());

        // Create a different transaction
        TransactionEntity differentTransaction = new TransactionEntity();
        differentTransaction.setId(9999L);

        // Test equality
        assertEquals(transaction, transaction); // Reflexivity - same instance
        assertEquals(transaction, sameTransaction); // Different instance but same values
        assertNotEquals(transaction, differentTransaction); // Different values
        assertNotEquals(transaction, null); // Null comparison
        assertNotEquals(transaction, new Object()); // Different type

        // Test hashCode consistency with equals
        assertEquals(transaction.hashCode(), sameTransaction.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(transaction.hashCode(), differentTransaction.hashCode()); // Different objects have different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains essential field values
     */
    @Test
    public void testToString() {
        String transactionString = transaction.toString();

        // Verify toString contains all important field values
        assertTrue(transactionString.contains(transaction.getId().toString()));
        assertTrue(transactionString.contains(debtorAccount));
        assertTrue(transactionString.contains("BE15203672485394")); // Grapes account
        assertTrue(transactionString.contains(debtorBank));
        assertTrue(transactionString.contains("Bank of Grapes"));
        assertTrue(transactionString.contains("Payment"));
        assertTrue(transactionString.contains(transferAmount.toString()));
        assertTrue(transactionString.contains(merchantName));
        assertTrue(transactionString.contains("Initiated"));
    }

    /**
     * Tests ID generation logic in parameterized constructor
     * Expected: Each transaction gets a unique ID
     */
    @Test
    public void testTransactionIdGeneration() {
        // Create multiple transactions with the same input parameters
        TransactionEntity transaction1 = new TransactionEntity(
                debtorAccount, debtorBank, clientId, clientAccountNumber,
                transferAmount, merchantName, merchantBusinessSector
        );

        TransactionEntity transaction2 = new TransactionEntity(
                debtorAccount, debtorBank, clientId, clientAccountNumber,
                transferAmount, merchantName, merchantBusinessSector
        );

        TransactionEntity transaction3 = new TransactionEntity(
                debtorAccount, debtorBank, clientId, clientAccountNumber,
                transferAmount, merchantName, merchantBusinessSector
        );

        // Verify each transaction received a different ID
        assertNotEquals(transaction1.getId(), transaction2.getId());
        assertNotEquals(transaction1.getId(), transaction3.getId());
        assertNotEquals(transaction2.getId(), transaction3.getId());
    }
}