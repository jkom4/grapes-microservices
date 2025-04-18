package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Account model
 * Tests constructors, getters, setters, equals, hashCode, and toString methods
 */
public class AccountTest {

    private Account account;

    @Mock
    private Client mockClient;

    @Mock
    private Bank mockBank;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a test account object with mock dependencies
        account = new Account();
        account.setAccountNumber("ACC123456789");
        account.setClient(mockClient);
        account.setBank(mockBank);
        account.setAuthenticationType("PIN");
        account.setBalance(new BigDecimal("1000.00"));
        account.setOpeningDate(LocalDate.of(2023, 1, 15));
        account.setAccountType("SAVINGS");
        account.setStatus("ACTIVE");
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty Account with all fields null
     */
    @Test
    public void testNoArgsConstructor() {
        Account account = new Account();
        assertNotNull(account);
        assertNull(account.getAccountNumber());
        assertNull(account.getClient());
        assertNull(account.getBank());
        assertNull(account.getAuthenticationType());
        assertNull(account.getBalance());
        assertNull(account.getOpeningDate());
        assertNull(account.getAccountType());
        assertNull(account.getStatus());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates an Account with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        Account newAccount = new Account(
                "ACC987654321",
                mockClient,
                mockBank,
                "OTP",
                new BigDecimal("2000.00"),
                LocalDate.of(2022, 5, 10),
                "CHECKING",
                "SUSPENDED"
        );

        assertEquals("ACC987654321", newAccount.getAccountNumber());
        assertEquals(mockClient, newAccount.getClient());
        assertEquals(mockBank, newAccount.getBank());
        assertEquals("OTP", newAccount.getAuthenticationType());
        assertEquals(new BigDecimal("2000.00"), newAccount.getBalance());
        assertEquals(LocalDate.of(2022, 5, 10), newAccount.getOpeningDate());
        assertEquals("CHECKING", newAccount.getAccountType());
        assertEquals("SUSPENDED", newAccount.getStatus());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        // Test getters
        assertEquals("ACC123456789", account.getAccountNumber());
        assertEquals(mockClient, account.getClient());
        assertEquals(mockBank, account.getBank());
        assertEquals("PIN", account.getAuthenticationType());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(LocalDate.of(2023, 1, 15), account.getOpeningDate());
        assertEquals("SAVINGS", account.getAccountType());
        assertEquals("ACTIVE", account.getStatus());

        // Test setters
        account.setAccountNumber("ACC000000000");
        account.setAuthenticationType("BIOMETRIC");
        account.setBalance(new BigDecimal("1500.00"));
        account.setOpeningDate(LocalDate.of(2024, 3, 20));
        account.setAccountType("FIXED_DEPOSIT");
        account.setStatus("CLOSED");

        assertEquals("ACC000000000", account.getAccountNumber());
        assertEquals("BIOMETRIC", account.getAuthenticationType());
        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        assertEquals(LocalDate.of(2024, 3, 20), account.getOpeningDate());
        assertEquals("FIXED_DEPOSIT", account.getAccountType());
        assertEquals("CLOSED", account.getStatus());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields, not just account number
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create an account with the same account number but other fields null
        Account sameAccount = new Account();
        sameAccount.setAccountNumber("ACC123456789");

        // Create account with different account number
        Account differentAccount = new Account();
        differentAccount.setAccountNumber("DIFFERENT123");

        // Test reflexivity
        assertEquals(account, account); // Same instance

        // These assertions pass because with Lombok @Data, all fields are compared
        // sameAccount has most fields null unlike our fully-populated account
        assertNotEquals(account, sameAccount);
        assertNotEquals(account, differentAccount);
        assertNotEquals(account, null);
        assertNotEquals(account, new Object());

        // Create a full copy with all fields identical to test true equality
        Account fullCopyAccount = new Account();
        fullCopyAccount.setAccountNumber("ACC123456789");
        fullCopyAccount.setClient(mockClient);
        fullCopyAccount.setBank(mockBank);
        fullCopyAccount.setAuthenticationType("PIN");
        fullCopyAccount.setBalance(new BigDecimal("1000.00"));
        fullCopyAccount.setOpeningDate(LocalDate.of(2023, 1, 15));
        fullCopyAccount.setAccountType("SAVINGS");
        fullCopyAccount.setStatus("ACTIVE");

        assertEquals(account, fullCopyAccount);

        // Test hashCode consistency with equals
        assertNotEquals(account.hashCode(), sameAccount.hashCode()); // Different fields = different hashCodes
        assertEquals(account.hashCode(), fullCopyAccount.hashCode()); // Same fields = same hashCodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains all field values
     */
    @Test
    public void testToString() {
        String toString = account.toString();

        // Verify the toString contains all important field values
        assertTrue(toString.contains("ACC123456789"));
        assertTrue(toString.contains("PIN"));
        assertTrue(toString.contains("1000"));
        assertTrue(toString.contains("SAVINGS"));
        assertTrue(toString.contains("ACTIVE"));
    }
}