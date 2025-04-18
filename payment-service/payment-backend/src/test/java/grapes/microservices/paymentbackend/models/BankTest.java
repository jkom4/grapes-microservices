package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Bank model
 * Tests constructors, getters, setters, equals, hashCode, and toString methods
 */
public class BankTest {

    private Bank bank;

    @BeforeEach
    public void setUp() {
        bank = new Bank("TestBank", "123 Banking Street", "TestCountry");
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty Bank with all fields null
     */
    @Test
    public void testNoArgsConstructor() {
        Bank emptyBank = new Bank();

        assertNotNull(emptyBank);
        assertNull(emptyBank.getBankName());
        assertNull(emptyBank.getBankAddress());
        assertNull(emptyBank.getCountry());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates a Bank with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        Bank newBank = new Bank("NewBank", "456 Finance Avenue", "OtherCountry");

        assertEquals("NewBank", newBank.getBankName());
        assertEquals("456 Finance Avenue", newBank.getBankAddress());
        assertEquals("OtherCountry", newBank.getCountry());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        // Test initial values
        assertEquals("TestBank", bank.getBankName());
        assertEquals("123 Banking Street", bank.getBankAddress());
        assertEquals("TestCountry", bank.getCountry());

        // Test setters
        bank.setBankName("UpdatedBank");
        bank.setBankAddress("789 Updated Road");
        bank.setCountry("UpdatedCountry");

        // Test updated values
        assertEquals("UpdatedBank", bank.getBankName());
        assertEquals("789 Updated Road", bank.getBankAddress());
        assertEquals("UpdatedCountry", bank.getCountry());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields, not just the bank name
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create a copy with the same values
        Bank sameBank = new Bank("TestBank", "123 Banking Street", "TestCountry");

        // Create a bank with different values
        Bank differentBank = new Bank("DifferentBank", "Different Address", "DifferentCountry");

        // Test equality
        assertEquals(bank, bank); // Reflexivity - same instance
        assertEquals(bank, sameBank); // Different instance but same values
        assertNotEquals(bank, differentBank); // Different values
        assertNotEquals(bank, null); // Null comparison
        assertNotEquals(bank, new Object()); // Different type

        // Create a bank with same bankName but different other fields
        Bank sameBankNameDiffFields = new Bank("TestBank", "Different Address", "DifferentCountry");

        // With Lombok @Data, equals checks all fields, not just the bank name
        assertNotEquals(bank, sameBankNameDiffFields);

        // Test hashCode consistency with equals
        assertEquals(bank.hashCode(), sameBank.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(bank.hashCode(), differentBank.hashCode()); // Different objects have different hashcodes
        assertNotEquals(bank.hashCode(), sameBankNameDiffFields.hashCode()); // Different fields = different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains all field values
     */
    @Test
    public void testToString() {
        String bankString = bank.toString();

        // Verify the toString contains all important fields
        assertTrue(bankString.contains("TestBank"));
        assertTrue(bankString.contains("123 Banking Street"));
        assertTrue(bankString.contains("TestCountry"));
    }
}