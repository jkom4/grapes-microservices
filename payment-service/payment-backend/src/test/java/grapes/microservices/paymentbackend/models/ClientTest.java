package grapes.microservices.paymentbackend.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Client model
 * Tests constructors, business methods (like getFullName), getters, setters,
 * and standard object methods
 */
public class ClientTest {

    private Client client;

    @Mock
    private Account mockAccount;

    @Mock
    private Card mockCard;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a fully populated client with mock dependencies
        client = new Client();
        client.setId(1L);
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setGender("Male");
        client.setBirthDate(LocalDate.of(1985, 5, 15));
        client.setMaritalStatus("Single");
        client.setAverageMonthlySalary(new BigDecimal("5000.00"));
        client.setEmail("john.doe@example.com");
        client.setPassword("hashedPassword123");
        client.setNationalRegistryNumber("85051512345");
        client.setPhoneNumber("+1234567890");
        client.setAddress("123 Main Street, Anytown");
        client.setRegistrationDate(LocalDate.of(2023, 1, 10));
        client.setStatus("active");

        List<Account> accounts = new ArrayList<>();
        accounts.add(mockAccount);
        client.setAccounts(accounts);

        List<Card> cards = new ArrayList<>();
        cards.add(mockCard);
        client.setCards(cards);
    }

    /**
     * Tests the no-args constructor
     * Expected: Creates an empty Client with all fields null
     */
    @Test
    public void testNoArgsConstructor() {
        Client emptyClient = new Client();

        assertNotNull(emptyClient);
        assertNull(emptyClient.getId());
        assertNull(emptyClient.getFirstName());
        assertNull(emptyClient.getLastName());
        assertNull(emptyClient.getGender());
        assertNull(emptyClient.getBirthDate());
        assertNull(emptyClient.getMaritalStatus());
        assertNull(emptyClient.getAverageMonthlySalary());
        assertNull(emptyClient.getEmail());
        assertNull(emptyClient.getPassword());
        assertNull(emptyClient.getNationalRegistryNumber());
        assertNull(emptyClient.getPhoneNumber());
        assertNull(emptyClient.getAddress());
        assertNull(emptyClient.getRegistrationDate());
        assertNull(emptyClient.getStatus());
        assertNull(emptyClient.getAccounts());
        assertNull(emptyClient.getCards());
    }

    /**
     * Tests the all-args constructor
     * Expected: Creates a Client with all fields set to specified values
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDate birthDate = LocalDate.of(1990, 3, 20);
        LocalDate registrationDate = LocalDate.of(2022, 11, 5);
        List<Account> accounts = new ArrayList<>();
        accounts.add(mockAccount);
        List<Card> cards = new ArrayList<>();
        cards.add(mockCard);

        Client newClient = new Client(
                2L,
                "Smith",
                "Jane",
                "Female",
                birthDate,
                "Married",
                new BigDecimal("6000.00"),
                "jane.smith@example.com",
                "hashedPassword456",
                "90032012345",
                "+1987654321",
                "456 Second Ave, Othertown",
                registrationDate,
                "active",
                accounts,
                cards
        );

        assertEquals(2L, newClient.getId());
        assertEquals("Smith", newClient.getLastName());
        assertEquals("Jane", newClient.getFirstName());
        assertEquals("Female", newClient.getGender());
        assertEquals(birthDate, newClient.getBirthDate());
        assertEquals("Married", newClient.getMaritalStatus());
        assertEquals(new BigDecimal("6000.00"), newClient.getAverageMonthlySalary());
        assertEquals("jane.smith@example.com", newClient.getEmail());
        assertEquals("hashedPassword456", newClient.getPassword());
        assertEquals("90032012345", newClient.getNationalRegistryNumber());
        assertEquals("+1987654321", newClient.getPhoneNumber());
        assertEquals("456 Second Ave, Othertown", newClient.getAddress());
        assertEquals(registrationDate, newClient.getRegistrationDate());
        assertEquals("active", newClient.getStatus());
        assertEquals(accounts, newClient.getAccounts());
        assertEquals(cards, newClient.getCards());
    }

    /**
     * Tests the basic parameterized constructor (id, email, password, phoneNumber)
     * Expected: Sets only the specified fields, leaving others null
     */
    @Test
    public void testParameterizedConstructor() {
        Client basicClient = new Client(3L, "basic@example.com", "simplePassword", "+1555123456");

        // Verify specified fields are set
        assertEquals(3L, basicClient.getId());
        assertEquals("basic@example.com", basicClient.getEmail());
        assertEquals("simplePassword", basicClient.getPassword());
        assertEquals("+1555123456", basicClient.getPhoneNumber());

        // Verify other fields remain null
        assertNull(basicClient.getFirstName());
        assertNull(basicClient.getLastName());
        assertNull(basicClient.getGender());
        assertNull(basicClient.getBirthDate());
        assertNull(basicClient.getMaritalStatus());
        assertNull(basicClient.getAverageMonthlySalary());
        assertNull(basicClient.getNationalRegistryNumber());
        assertNull(basicClient.getAddress());
        assertNull(basicClient.getRegistrationDate());
        assertNull(basicClient.getStatus());
        assertNull(basicClient.getAccounts());
        assertNull(basicClient.getCards());
    }

    /**
     * Tests the getFullName method under various conditions
     * Expected: Concatenates first and last name, handling null values
     */
    @Test
    public void testGetFullName() {
        // Normal case
        assertEquals("John Doe", client.getFullName());

        // Different name values
        client.setFirstName("Jane");
        client.setLastName("Smith");
        assertEquals("Jane Smith", client.getFullName());

        // Edge cases with null values
        client.setFirstName(null);
        client.setLastName("Brown");
        assertEquals("null Brown", client.getFullName());

        client.setFirstName("Robert");
        client.setLastName(null);
        assertEquals("Robert null", client.getFullName());

        client.setFirstName(null);
        client.setLastName(null);
        assertEquals("null null", client.getFullName());
    }

    /**
     * Tests getter and setter methods
     * Expected: Get methods return the correct values, set methods update them properly
     */
    @Test
    public void testGettersAndSetters() {
        // Test setting new values (initial values already tested in setUp)
        LocalDate newBirthDate = LocalDate.of(1992, 8, 25);
        LocalDate newRegistrationDate = LocalDate.of(2021, 4, 1);

        client.setId(10L);
        client.setFirstName("Updated");
        client.setLastName("Name");
        client.setGender("Other");
        client.setBirthDate(newBirthDate);
        client.setMaritalStatus("Divorced");
        client.setAverageMonthlySalary(new BigDecimal("7500.00"));
        client.setEmail("updated.email@example.com");
        client.setPassword("newHashedPassword");
        client.setNationalRegistryNumber("9208251234");
        client.setPhoneNumber("+19876543210");
        client.setAddress("789 New Address Rd, Newtown");
        client.setRegistrationDate(newRegistrationDate);
        client.setStatus("suspended");

        List<Account> newAccounts = new ArrayList<>();
        client.setAccounts(newAccounts);

        List<Card> newCards = new ArrayList<>();
        client.setCards(newCards);

        // Verify updated values
        assertEquals(10L, client.getId());
        assertEquals("Updated", client.getFirstName());
        assertEquals("Name", client.getLastName());
        assertEquals("Other", client.getGender());
        assertEquals(newBirthDate, client.getBirthDate());
        assertEquals("Divorced", client.getMaritalStatus());
        assertEquals(new BigDecimal("7500.00"), client.getAverageMonthlySalary());
        assertEquals("updated.email@example.com", client.getEmail());
        assertEquals("newHashedPassword", client.getPassword());
        assertEquals("9208251234", client.getNationalRegistryNumber());
        assertEquals("+19876543210", client.getPhoneNumber());
        assertEquals("789 New Address Rd, Newtown", client.getAddress());
        assertEquals(newRegistrationDate, client.getRegistrationDate());
        assertEquals("suspended", client.getStatus());
        assertEquals(newAccounts, client.getAccounts());
        assertEquals(newCards, client.getCards());
    }

    /**
     * Tests equals and hashCode methods
     * Expected: With Lombok @Data, equals compares all fields
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create a copy with the same values
        Client sameClient = new Client();
        sameClient.setId(1L);
        sameClient.setFirstName("John");
        sameClient.setLastName("Doe");
        sameClient.setGender("Male");
        sameClient.setBirthDate(LocalDate.of(1985, 5, 15));
        sameClient.setMaritalStatus("Single");
        sameClient.setAverageMonthlySalary(new BigDecimal("5000.00"));
        sameClient.setEmail("john.doe@example.com");
        sameClient.setPassword("hashedPassword123");
        sameClient.setNationalRegistryNumber("85051512345");
        sameClient.setPhoneNumber("+1234567890");
        sameClient.setAddress("123 Main Street, Anytown");
        sameClient.setRegistrationDate(LocalDate.of(2023, 1, 10));
        sameClient.setStatus("active");
        sameClient.setAccounts(client.getAccounts());
        sameClient.setCards(client.getCards());

        // Create a different client
        Client differentClient = new Client();
        differentClient.setId(2L);
        differentClient.setEmail("different@example.com");

        // Test equality
        assertEquals(client, client); // Reflexivity - same instance
        assertEquals(client, sameClient); // Different instance but same values
        assertNotEquals(client, differentClient); // Different values
        assertNotEquals(client, null); // Null comparison
        assertNotEquals(client, new Object()); // Different type

        // Test hashCode consistency with equals
        assertEquals(client.hashCode(), sameClient.hashCode()); // Equal objects have equal hashcodes
        assertNotEquals(client.hashCode(), differentClient.hashCode()); // Different objects have different hashcodes
    }

    /**
     * Tests toString method
     * Expected: String representation contains essential field values
     */
    @Test
    public void testToString() {
        String clientString = client.toString();

        // Verify toString contains important fields
        assertTrue(clientString.contains("id=1"));
        assertTrue(clientString.contains("John"));
        assertTrue(clientString.contains("Doe"));
        assertTrue(clientString.contains("john.doe@example.com"));
    }
}