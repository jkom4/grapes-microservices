package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Merchant model
 * Tests constructors, getters, setters, equals, hashCode, and toString methods
 */
public class MerchantTest {

    private Merchant merchant;

    @BeforeEach
    public void setUp() {
        // Initialize a test merchant with all fields populated
        merchant = new Merchant();
        merchant.setMerchantName("TestShop");
        merchant.setBusinessSector("Retail");
        merchant.setMerchantAddress("123 Commerce St, Business City");
        merchant.setVatNumber("BE0123456789");
        merchant.setRegistrationDate(LocalDate.of(2023, 3, 15));
        merchant.setMerchantStatus("active");
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty Merchant with all fields null
     */
    @Test
    public void testNoArgsConstructor() {
        Merchant emptyMerchant = new Merchant();

        assertNotNull(emptyMerchant);
        assertNull(emptyMerchant.getMerchantName());
        assertNull(emptyMerchant.getBusinessSector());
        assertNull(emptyMerchant.getMerchantAddress());
        assertNull(emptyMerchant.getVatNumber());
        assertNull(emptyMerchant.getRegistrationDate());
        assertNull(emptyMerchant.getMerchantStatus());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates a Merchant with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDate testDate = LocalDate.of(2022, 5, 10);
        Merchant fullMerchant = new Merchant(
                "FullTestStore",
                "Technology",
                "456 Tech Avenue, Innovation Park",
                "BE9876543210",
                testDate,
                "pending"
        );

        assertEquals("FullTestStore", fullMerchant.getMerchantName());
        assertEquals("Technology", fullMerchant.getBusinessSector());
        assertEquals("456 Tech Avenue, Innovation Park", fullMerchant.getMerchantAddress());
        assertEquals("BE9876543210", fullMerchant.getVatNumber());
        assertEquals(testDate, fullMerchant.getRegistrationDate());
        assertEquals("pending", fullMerchant.getMerchantStatus());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        // Test initial values from setup
        assertEquals("TestShop", merchant.getMerchantName());
        assertEquals("Retail", merchant.getBusinessSector());
        assertEquals("123 Commerce St, Business City", merchant.getMerchantAddress());
        assertEquals("BE0123456789", merchant.getVatNumber());
        assertEquals(LocalDate.of(2023, 3, 15), merchant.getRegistrationDate());
        assertEquals("active", merchant.getMerchantStatus());

        // Test updating values with setters
        LocalDate newDate = LocalDate.of(2024, 1, 20);
        merchant.setMerchantName("UpdatedShop");
        merchant.setBusinessSector("Food & Beverage");
        merchant.setMerchantAddress("789 Restaurant Row, Foodie District");
        merchant.setVatNumber("BE5555555555");
        merchant.setRegistrationDate(newDate);
        merchant.setMerchantStatus("suspended");

        // Verify updated values
        assertEquals("UpdatedShop", merchant.getMerchantName());
        assertEquals("Food & Beverage", merchant.getBusinessSector());
        assertEquals("789 Restaurant Row, Foodie District", merchant.getMerchantAddress());
        assertEquals("BE5555555555", merchant.getVatNumber());
        assertEquals(newDate, merchant.getRegistrationDate());
        assertEquals("suspended", merchant.getMerchantStatus());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields, not just the merchant name
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create a merchant with identical field values
        Merchant sameMerchant = new Merchant(
                "TestShop",
                "Retail",
                "123 Commerce St, Business City",
                "BE0123456789",
                LocalDate.of(2023, 3, 15),
                "active"
        );

        // Create a merchant with completely different values
        Merchant differentMerchant = new Merchant(
                "OtherShop",
                "Services",
                "Different Address",
                "Different VAT",
                LocalDate.now(),
                "inactive"
        );

        // Test equality
        assertEquals(merchant, merchant); // Reflexivity - same instance
        assertEquals(merchant, sameMerchant); // Different instance but same values
        assertNotEquals(merchant, differentMerchant); // Different values
        assertNotEquals(merchant, null); // Null comparison
        assertNotEquals(merchant, new Object()); // Different type

        // Create a merchant with same name but different other fields
        Merchant sameMerchantNameDiffFields = new Merchant(
                "TestShop", // Same merchantName
                "Different Sector",
                "Different Address",
                "Different VAT",
                LocalDate.now(),
                "Different Status"
        );

        // With Lombok @Data, equals compares all fields, not just the merchant name
        assertNotEquals(merchant, sameMerchantNameDiffFields);

        // Test hashCode consistency with equals
        assertEquals(merchant.hashCode(), sameMerchant.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(merchant.hashCode(), differentMerchant.hashCode()); // Different objects have different hashcodes
        assertNotEquals(merchant.hashCode(), sameMerchantNameDiffFields.hashCode()); // Objects with different fields have different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains all field values
     */
    @Test
    public void testToString() {
        String merchantString = merchant.toString();

        // Verify the toString contains all important fields
        assertTrue(merchantString.contains("TestShop"));
        assertTrue(merchantString.contains("Retail"));
        assertTrue(merchantString.contains("123 Commerce St, Business City"));
        assertTrue(merchantString.contains("BE0123456789"));
        assertTrue(merchantString.contains("2023-03-15"));
        assertTrue(merchantString.contains("active"));
    }
}