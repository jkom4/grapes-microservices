package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.ClientDTO;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import grapes.microservices.paymentbackend.utils.PasswordManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for ClientService
 * Tests client management operations including authentication, account/card access,
 * client creation, and account balance updates
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    // Repository and utility dependencies
    @Mock private ClientRepository clientRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardRepository cardRepository;
    @Mock private PasswordManager passwordManager;

    // Mock objects for return values and parameters
    @Mock private Client mockClient;
    @Mock private Account mockAccount;
    @Mock private Card mockCard;
    @Mock private ClientDTO mockClientDTO;

    // Argument captors for validation
    @Captor private ArgumentCaptor<Client> clientCaptor;
    @Captor private ArgumentCaptor<Account> accountCaptor;

    // Test constants
    private final Long CLIENT_ID = 1L;
    private final String CLIENT_EMAIL = "test@example.com";
    private final String CLIENT_PASSWORD = "password123";
    private final String HASHED_PASSWORD = "hashedPassword";
    private final String CARD_NUMBER = "1234-5678-9012-3456";

    @BeforeEach
    void setUp() {
        // No global mock setup needed
    }

    /**
     * Tests finding a client by email when the client exists
     * Expected: Returns a populated Optional with the client
     */
    @Test
    void findByEmail_ClientExists_ReturnsOptionalClient() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(mockClient));

        // Act
        Optional<Client> result = clientService.findByEmail(CLIENT_EMAIL);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
        verify(clientRepository, times(1)).findByEmail(eq(CLIENT_EMAIL));
    }

    /**
     * Tests finding a client by email when the client doesn't exist
     * Expected: Returns an empty Optional
     */
    @Test
    void findByEmail_ClientDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.findByEmail(CLIENT_EMAIL);

        // Assert
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findByEmail(eq(CLIENT_EMAIL));
    }

    /**
     * Tests finding a client by ID when the client exists
     * Expected: Returns a populated Optional with the client
     */
    @Test
    void findById_ClientExists_ReturnsOptionalClient() {
        // Arrange
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mockClient));

        // Act
        Optional<Client> result = clientService.findById(CLIENT_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
        verify(clientRepository, times(1)).findById(eq(CLIENT_ID));
    }

    /**
     * Tests finding a client by ID when the client doesn't exist
     * Expected: Returns an empty Optional
     */
    @Test
    void findById_ClientDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.findById(CLIENT_ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findById(eq(CLIENT_ID));
    }

    /**
     * Tests credential verification with valid credentials
     * Expected: Returns true when email exists and password matches
     */
    @Test
    void verifyCredentials_ValidCredentials_ReturnsTrue() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(mockClient));
        when(mockClient.getPassword()).thenReturn(HASHED_PASSWORD); // Client has this hashed password
        when(passwordManager.matches(CLIENT_PASSWORD, HASHED_PASSWORD)).thenReturn(true); // Password matches

        // Act
        boolean result = clientService.verifyCredentials(CLIENT_EMAIL, CLIENT_PASSWORD);

        // Assert
        assertTrue(result);
        verify(clientRepository, times(1)).findByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, times(1)).matches(eq(CLIENT_PASSWORD), eq(HASHED_PASSWORD));
    }

    /**
     * Tests credential verification when client is not found
     * Expected: Returns false when email doesn't exist
     */
    @Test
    void verifyCredentials_ClientNotFound_ReturnsFalse() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.verifyCredentials(CLIENT_EMAIL, CLIENT_PASSWORD);

        // Assert
        assertFalse(result);
        verify(clientRepository, times(1)).findByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, never()).matches(anyString(), anyString()); // PasswordManager not called
    }

    /**
     * Tests credential verification with incorrect password
     * Expected: Returns false when password doesn't match
     */
    @Test
    void verifyCredentials_IncorrectPassword_ReturnsFalse() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(mockClient));
        when(mockClient.getPassword()).thenReturn(HASHED_PASSWORD);
        when(passwordManager.matches(CLIENT_PASSWORD, HASHED_PASSWORD)).thenReturn(false); // Password doesn't match

        // Act
        boolean result = clientService.verifyCredentials(CLIENT_EMAIL, CLIENT_PASSWORD);

        // Assert
        assertFalse(result);
        verify(clientRepository, times(1)).findByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, times(1)).matches(eq(CLIENT_PASSWORD), eq(HASHED_PASSWORD));
    }

    /**
     * Tests retrieving a client's cards when they have cards
     * Expected: Returns list of cards
     */
    @Test
    void getClientCards_ClientHasCards_ReturnsCardList() {
        // Arrange
        List<Card> expectedCards = List.of(mockCard);
        when(cardRepository.findByClientId(CLIENT_ID)).thenReturn(expectedCards);

        // Act
        List<Card> result = clientService.getClientCards(CLIENT_ID);

        // Assert
        assertEquals(expectedCards, result);
        verify(cardRepository, times(1)).findByClientId(eq(CLIENT_ID));
    }

    /**
     * Tests retrieving a client's cards when they have no cards
     * Expected: Returns empty list
     */
    @Test
    void getClientCards_ClientHasNoCards_ReturnsEmptyList() {
        // Arrange
        when(cardRepository.findByClientId(CLIENT_ID)).thenReturn(Collections.emptyList());

        // Act
        List<Card> result = clientService.getClientCards(CLIENT_ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(cardRepository, times(1)).findByClientId(eq(CLIENT_ID));
    }

    /**
     * Tests retrieving a client's accounts when they have accounts
     * Expected: Returns list of accounts
     */
    @Test
    void getClientAccounts_ClientHasAccounts_ReturnsAccountList() {
        // Arrange
        List<Account> expectedAccounts = List.of(mockAccount);
        when(accountRepository.findByClientId(CLIENT_ID)).thenReturn(expectedAccounts);

        // Act
        List<Account> result = clientService.getClientAccounts(CLIENT_ID);

        // Assert
        assertEquals(expectedAccounts, result);
        verify(accountRepository, times(1)).findByClientId(eq(CLIENT_ID));
    }

    /**
     * Tests retrieving a client's accounts when they have no accounts
     * Expected: Returns empty list
     */
    @Test
    void getClientAccounts_ClientHasNoAccounts_ReturnsEmptyList() {
        // Arrange
        when(accountRepository.findByClientId(CLIENT_ID)).thenReturn(Collections.emptyList());

        // Act
        List<Account> result = clientService.getClientAccounts(CLIENT_ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(accountRepository, times(1)).findByClientId(eq(CLIENT_ID));
    }

    /**
     * Tests client creation with valid data
     * Expected: Successfully creates and saves client with proper values
     */
    @Test
    void createClient_ValidDto_CreatesAndSavesClient() throws NoSuchAlgorithmException {
        // Arrange
        when(mockClientDTO.getEmail()).thenReturn(CLIENT_EMAIL);
        when(mockClientDTO.getPassword()).thenReturn(CLIENT_PASSWORD);
        // Other DTO getters (assuming they return valid values)
        when(mockClientDTO.getPhoneNumber()).thenReturn("+123");
        when(mockClientDTO.getFirstName()).thenReturn("Test");

        when(clientRepository.existsByEmail(CLIENT_EMAIL)).thenReturn(false); // Email doesn't exist
        when(passwordManager.saltPassword(CLIENT_PASSWORD)).thenReturn("salted_" + CLIENT_PASSWORD);
        when(passwordManager.hashPassword("salted_" + CLIENT_PASSWORD)).thenReturn(HASHED_PASSWORD);
        // Simulate save: return the Client object passed to it
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Client createdClient = clientService.createClient(mockClientDTO);

        // Assert
        assertNotNull(createdClient);
        assertEquals(CLIENT_EMAIL, createdClient.getEmail());
        assertEquals(HASHED_PASSWORD, createdClient.getPassword()); // Verify hashed password
        assertEquals("Active", createdClient.getStatus()); // Verify default status
        assertNotNull(createdClient.getRegistrationDate()); // Verify date

        // Verify
        verify(clientRepository, times(1)).existsByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, times(1)).saltPassword(eq(CLIENT_PASSWORD));
        verify(passwordManager, times(1)).hashPassword(eq("salted_" + CLIENT_PASSWORD));
        verify(clientRepository, times(1)).save(clientCaptor.capture()); // Capture saved object

        // Verify details of captured client
        Client savedClient = clientCaptor.getValue();
        assertEquals(CLIENT_EMAIL, savedClient.getEmail());
        assertEquals(HASHED_PASSWORD, savedClient.getPassword());
        assertEquals("Test", savedClient.getFirstName()); // Verify another copied property
    }

    /**
     * Tests client creation when email already exists
     * Expected: Throws IllegalArgumentException
     */
    @Test
    void createClient_EmailAlreadyExists_ThrowsIllegalArgumentException() throws NoSuchAlgorithmException {
        // Arrange
        when(mockClientDTO.getEmail()).thenReturn(CLIENT_EMAIL);
        when(clientRepository.existsByEmail(CLIENT_EMAIL)).thenReturn(true); // Email already exists

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.createClient(mockClientDTO);
        });
        assertTrue(exception.getMessage().contains("already exists"));

        // Verify
        verify(clientRepository, times(1)).existsByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, never()).saltPassword(anyString());
        verify(passwordManager, never()).hashPassword(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    /**
     * Tests client creation when password hashing fails
     * Expected: Throws NoSuchAlgorithmException
     */
    @Test
    void createClient_PasswordHashingFails_ThrowsException() throws NoSuchAlgorithmException {
        // Arrange
        when(mockClientDTO.getEmail()).thenReturn(CLIENT_EMAIL);
        when(mockClientDTO.getPassword()).thenReturn(CLIENT_PASSWORD);
        when(clientRepository.existsByEmail(CLIENT_EMAIL)).thenReturn(false);
        when(passwordManager.saltPassword(CLIENT_PASSWORD)).thenReturn("salted_" + CLIENT_PASSWORD);
        // Simulate hashing failure
        when(passwordManager.hashPassword("salted_" + CLIENT_PASSWORD)).thenThrow(new NoSuchAlgorithmException("Hashing failed"));

        // Act & Assert
        assertThrows(NoSuchAlgorithmException.class, () -> {
            clientService.createClient(mockClientDTO);
        });

        // Verify
        verify(clientRepository, times(1)).existsByEmail(eq(CLIENT_EMAIL));
        verify(passwordManager, times(1)).saltPassword(eq(CLIENT_PASSWORD));
        verify(passwordManager, times(1)).hashPassword(eq("salted_" + CLIENT_PASSWORD));
        verify(clientRepository, never()).save(any(Client.class)); // No save
    }

    /**
     * Tests account balance update with sufficient funds
     * Expected: Updates balance and returns true
     */
    @Test
    void updateAccountBalance_SufficientBalance_UpdatesAndReturnsTrue() {
        // Arrange
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal amountToSubtract = new BigDecimal("30.50");
        BigDecimal expectedNewBalance = new BigDecimal("69.50");

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mockClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(CLIENT_ID)).thenReturn(Optional.of(mockAccount));
        when(mockAccount.getBalance()).thenReturn(initialBalance);
        when(mockAccount.getAccountNumber()).thenReturn("ACC123"); // For logging

        // Act
        boolean result = clientService.updateAccountBalance(CLIENT_ID, amountToSubtract);

        // Assert
        assertTrue(result);

        // Verify
        verify(clientRepository, times(1)).findById(eq(CLIENT_ID));
        verify(accountRepository, times(1)).findFirstByClientIdOrderByOpeningDateDesc(eq(CLIENT_ID));
        verify(mockAccount, times(1)).setBalance(eq(expectedNewBalance)); // Verify setBalance called with correct value
        verify(accountRepository, times(1)).save(eq(mockAccount)); // Verify modified account is saved
    }

    /**
     * Tests account balance update when client is not found
     * Expected: Returns false without updating
     */
    @Test
    void updateAccountBalance_ClientNotFound_ReturnsFalse() {
        // Arrange
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty()); // Client not found

        // Act
        boolean result = clientService.updateAccountBalance(CLIENT_ID, new BigDecimal("10.00"));

        // Assert
        assertFalse(result);

        // Verify
        verify(accountRepository, never()).findFirstByClientIdOrderByOpeningDateDesc(anyLong());
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Tests account balance update when account is not found
     * Expected: Returns false without updating
     */
    @Test
    void updateAccountBalance_AccountNotFound_ReturnsFalse() {
        // Arrange
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mockClient)); // Client found
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(CLIENT_ID)).thenReturn(Optional.empty()); // Account not found

        // Act
        boolean result = clientService.updateAccountBalance(CLIENT_ID, new BigDecimal("10.00"));

        // Assert
        assertFalse(result);

        // Verify
        verify(accountRepository, times(1)).findFirstByClientIdOrderByOpeningDateDesc(eq(CLIENT_ID));
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Tests account balance update with insufficient funds
     * Expected: Returns false without updating
     */
    @Test
    void updateAccountBalance_InsufficientBalance_ReturnsFalse() {
        // Arrange
        BigDecimal initialBalance = new BigDecimal("20.00");
        BigDecimal amountToSubtract = new BigDecimal("30.50");

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mockClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(CLIENT_ID)).thenReturn(Optional.of(mockAccount));
        when(mockAccount.getBalance()).thenReturn(initialBalance); // Insufficient balance

        // Act
        boolean result = clientService.updateAccountBalance(CLIENT_ID, amountToSubtract);

        // Assert
        assertFalse(result);

        // Verify
        verify(mockAccount, never()).setBalance(any()); // setBalance should not be called
        verify(accountRepository, never()).save(any(Account.class)); // No save
    }

    /**
     * Tests account balance update when balance is null
     * Expected: Returns false without updating
     */
    @Test
    void updateAccountBalance_NullBalance_ReturnsFalse() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("30.50");
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mockClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(CLIENT_ID)).thenReturn(Optional.of(mockAccount));
        when(mockAccount.getBalance()).thenReturn(null); // Null balance

        // Act
        boolean result = clientService.updateAccountBalance(CLIENT_ID, amountToSubtract);

        // Assert
        assertFalse(result);

        // Verify
        verify(mockAccount, never()).setBalance(any());
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Tests card ownership verification when card belongs to client
     * Expected: Returns true
     */
    @Test
    void isCardOwnedByClient_CardOwned_ReturnsTrue() {
        // Arrange
        when(cardRepository.findByCardNumberAndClientId(CARD_NUMBER, CLIENT_ID)).thenReturn(Optional.of(mockCard));

        // Act
        boolean result = clientService.isCardOwnedByClient(CARD_NUMBER, CLIENT_ID);

        // Assert
        assertTrue(result);
        verify(cardRepository, times(1)).findByCardNumberAndClientId(eq(CARD_NUMBER), eq(CLIENT_ID));
    }

    /**
     * Tests card ownership verification when card does not belong to client
     * Expected: Returns false
     */
    @Test
    void isCardOwnedByClient_CardNotOwned_ReturnsFalse() {
        // Arrange
        when(cardRepository.findByCardNumberAndClientId(CARD_NUMBER, CLIENT_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.isCardOwnedByClient(CARD_NUMBER, CLIENT_ID);

        // Assert
        assertFalse(result);
        verify(cardRepository, times(1)).findByCardNumberAndClientId(eq(CARD_NUMBER), eq(CLIENT_ID));
    }
}