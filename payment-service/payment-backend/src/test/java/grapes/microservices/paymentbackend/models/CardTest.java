package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Card model
 * Tests constructors, masking functionality, getters, setters, and object methods
 */
public class CardTest {

    private Card card;

    @Mock
    private Client mockClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        card = new Card(
                mockClient,
                "4111111111111111",
                "12/25",
                "John Doe"
        );
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty Card with all fields null/default
     */
    @Test
    public void testNoArgsConstructor() {
        Card emptyCard = new Card();

        assertNotNull(emptyCard);
        assertNull(emptyCard.getId());
        assertNull(emptyCard.getClient());
        assertNull(emptyCard.getCardNumber());
        assertNull(emptyCard.getExpirationDate());
        assertNull(emptyCard.getCardholderName());
        assertNull(emptyCard.getCardType());
        assertNull(emptyCard.getStatus());
        assertNull(emptyCard.getAddedDate());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates a Card with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDate testDate = LocalDate.of(2023, 5, 15);
        Card fullCard = new Card(
                1L,
                mockClient,
                "5555555555554444",
                "03/27",
                "Jane Smith",
                "MASTERCARD",
                "active",
                testDate
        );

        assertEquals(1L, fullCard.getId());
        assertEquals(mockClient, fullCard.getClient());
        assertEquals("5555555555554444", fullCard.getCardNumber());
        assertEquals("03/27", fullCard.getExpirationDate());
        assertEquals("Jane Smith", fullCard.getCardholderName());
        assertEquals("MASTERCARD", fullCard.getCardType());
        assertEquals("active", fullCard.getStatus());
        assertEquals(testDate, fullCard.getAddedDate());
    }

    /**
     * Tests the parameterized constructor (client, cardNumber, expirationDate, cardholderName)
     * Expected: Sets required fields and initializes defaults for other fields
     */
    @Test
    public void testParameterizedConstructor() {
        // Verify required fields are set correctly
        assertEquals(mockClient, card.getClient());
        assertEquals("4111111111111111", card.getCardNumber());
        assertEquals("12/25", card.getExpirationDate());
        assertEquals("John Doe", card.getCardholderName());

        // Verify default values
        assertNull(card.getId());
        assertEquals("active", card.getStatus());
        assertNotNull(card.getAddedDate());
        assertEquals(LocalDate.now(), card.getAddedDate());
        assertNull(card.getCardType());
    }

    /**
     * Tests the card number masking functionality
     * Expected: Masks card numbers appropriately for different scenarios
     */
    @Test
    public void testGetMaskedCardNumber() {
        // Standard 16-digit card number
        assertEquals("************1111", card.getMaskedCardNumber());

        // Different card number
        card.setCardNumber("378282246310005");
        assertEquals("************0005", card.getMaskedCardNumber());

        // Null card number
        card.setCardNumber(null);
        assertEquals("****", card.getMaskedCardNumber());

        // Short card number (less than 4 digits)
        card.setCardNumber("123");
        assertEquals("****", card.getMaskedCardNumber());

        // Exactly 4 digits
        card.setCardNumber("1234");
        assertEquals("************1234", card.getMaskedCardNumber());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        LocalDate newDate = LocalDate.of(2022, 10, 5);

        card.setId(100L);
        card.setCardNumber("378282246310005");
        card.setExpirationDate("09/28");
        card.setCardholderName("Alice Johnson");
        card.setCardType("AMEX");
        card.setStatus("blocked");
        card.setAddedDate(newDate);

        assertEquals(100L, card.getId());
        assertEquals("378282246310005", card.getCardNumber());
        assertEquals("09/28", card.getExpirationDate());
        assertEquals("Alice Johnson", card.getCardholderName());
        assertEquals("AMEX", card.getCardType());
        assertEquals("blocked", card.getStatus());
        assertEquals(newDate, card.getAddedDate());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create a copy with the same values
        Card sameCard = new Card();
        sameCard.setId(null);
        sameCard.setClient(mockClient);
        sameCard.setCardNumber("4111111111111111");
        sameCard.setExpirationDate("12/25");
        sameCard.setCardholderName("John Doe");
        sameCard.setStatus("active");
        sameCard.setAddedDate(card.getAddedDate());

        // Create a different card
        Card differentCard = new Card();
        differentCard.setCardNumber("5555555555554444");

        // Test equality
        assertEquals(card, card); // Reflexivity - same instance
        assertEquals(card, sameCard); // Different instance but same values
        assertNotEquals(card, differentCard); // Different values
        assertNotEquals(card, null); // Null comparison
        assertNotEquals(card, new Object()); // Different type

        // Test hashCode consistency with equals
        assertEquals(card.hashCode(), sameCard.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(card.hashCode(), differentCard.hashCode()); // Different objects have different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains essential field values
     */
    @Test
    public void testToString() {
        String cardString = card.toString();

        // Verify toString contains all important fields
        assertTrue(cardString.contains("4111111111111111"));
        assertTrue(cardString.contains("12/25"));
        assertTrue(cardString.contains("John Doe"));
        assertTrue(cardString.contains("active"));
    }
}