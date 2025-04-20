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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PasswordManager passwordManager;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private Client testClient;
    private Account testAccount;
    private Card testCard;
    private ClientDTO testClientDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testClient = new Client();
        testClient.setId(1L);
        testClient.setEmail("test@example.com");
        testClient.setFirstName("Test");
        testClient.setLastName("User");
        testClient.setPassword("hashedPassword");
        testClient.setStatus("Active");
        testClient.setRegistrationDate(LocalDate.now());
        testClient.setPhoneNumber("+15555555555");

        testAccount = new Account();
        testAccount.setAccountNumber("ACC123456789");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setClient(testClient);
        testAccount.setOpeningDate(LocalDate.now());
        testAccount.setStatus("Active");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("4111111111111111");
        testCard.setExpirationDate("12/25");
        testCard.setClient(testClient);

        testClientDTO = new ClientDTO();
        testClientDTO.setEmail("new@example.com");
        testClientDTO.setFirstName("New");
        testClientDTO.setLastName("User");
        testClientDTO.setPassword("password");
        testClientDTO.setPhoneNumber("+15555555556");
    }

    @Test
    void findByEmail_ShouldReturnClient_WhenEmailExists() {
        // Arrange
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // Act
        Optional<Client> result = clientService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testClient, result.get());
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        // Arrange
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void findById_ShouldReturnClient_WhenIdExists() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        // Act
        Optional<Client> result = clientService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testClient, result.get());
        verify(clientRepository).findById(1L);
    }

    @Test
    void verifyCredentials_ShouldReturnTrue_WhenCredentialsAreValid() {
        // Arrange
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(passwordManager.matches("password", "hashedPassword")).thenReturn(true);

        // Act
        boolean result = clientService.verifyCredentials("test@example.com", "password");

        // Assert
        assertTrue(result);
        verify(clientRepository).findByEmail("test@example.com");
        verify(passwordManager).matches("password", "hashedPassword");
    }

    @Test
    void verifyCredentials_ShouldReturnFalse_WhenEmailNotFound() {
        // Arrange
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.verifyCredentials("nonexistent@example.com", "password");

        // Assert
        assertFalse(result);
        verify(clientRepository).findByEmail("nonexistent@example.com");
        verify(passwordManager, never()).matches(anyString(), anyString());
    }

    @Test
    void verifyCredentials_ShouldReturnFalse_WhenPasswordIsInvalid() {
        // Arrange
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(passwordManager.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // Act
        boolean result = clientService.verifyCredentials("test@example.com", "wrongPassword");

        // Assert
        assertFalse(result);
        verify(clientRepository).findByEmail("test@example.com");
        verify(passwordManager).matches("wrongPassword", "hashedPassword");
    }

    @Test
    void getClientCards_ShouldReturnCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByClientId(1L)).thenReturn(cards);

        // Act
        List<Card> result = clientService.getClientCards(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testCard, result.get(0));
        verify(cardRepository).findByClientId(1L);
    }

    @Test
    void getClientAccounts_ShouldReturnAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountRepository.findByClientId(1L)).thenReturn(accounts);

        // Act
        List<Account> result = clientService.getClientAccounts(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testAccount, result.get(0));
        verify(accountRepository).findByClientId(1L);
    }

    @Test
    void createClient_ShouldCreateAndReturnClient() throws NoSuchAlgorithmException {
        // Arrange
        when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordManager.saltPassword("password")).thenReturn("salted_password");
        when(passwordManager.hashPassword("salted_password")).thenReturn("hashed_password");
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Client result = clientService.createClient(testClientDTO);

        // Assert
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("hashed_password", result.getPassword());
        assertEquals("Active", result.getStatus());
        assertEquals(LocalDate.now(), result.getRegistrationDate());

        verify(passwordManager).saltPassword("password");
        verify(passwordManager).hashPassword("salted_password");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClient_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(clientRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(testClientDTO));
        assertEquals("Client with email new@example.com already exists", exception.getMessage());

        verify(clientRepository).existsByEmail("new@example.com");
        verify(passwordManager, never()).saltPassword(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateAccountBalance_ShouldReturnTrue_WhenBalanceUpdatedSuccessfully() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("500.00");
        BigDecimal expectedNewBalance = new BigDecimal("500.00");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        boolean result = clientService.updateAccountBalance(1L, amountToSubtract);

        // Assert
        assertTrue(result);
        assertEquals(expectedNewBalance, testAccount.getBalance());

        verify(clientRepository).findById(1L);
        verify(accountRepository).findFirstByClientIdOrderByOpeningDateDesc(1L);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void updateAccountBalance_ShouldReturnFalse_WhenClientNotFound() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("500.00");
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.updateAccountBalance(999L, amountToSubtract);

        // Assert
        assertFalse(result);
        verify(clientRepository).findById(999L);
        verify(accountRepository, never()).findFirstByClientIdOrderByOpeningDateDesc(anyLong());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccountBalance_ShouldReturnFalse_WhenAccountNotFound() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("500.00");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.updateAccountBalance(1L, amountToSubtract);

        // Assert
        assertFalse(result);
        verify(clientRepository).findById(1L);
        verify(accountRepository).findFirstByClientIdOrderByOpeningDateDesc(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccountBalance_ShouldReturnFalse_WhenInsufficientBalance() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("1500.00"); // More than available balance
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(accountRepository.findFirstByClientIdOrderByOpeningDateDesc(1L)).thenReturn(Optional.of(testAccount));

        // Act
        boolean result = clientService.updateAccountBalance(1L, amountToSubtract);

        // Assert
        assertFalse(result);
        assertEquals(new BigDecimal("1000.00"), testAccount.getBalance()); // Balance should remain unchanged

        verify(clientRepository).findById(1L);
        verify(accountRepository).findFirstByClientIdOrderByOpeningDateDesc(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void isCardOwnedByClient_ShouldReturnTrue_WhenCardBelongsToClient() {
        // Arrange
        String cardNumber = "4111111111111111";
        Long clientId = 1L;
        when(cardRepository.findByCardNumberAndClientId(cardNumber, clientId)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = clientService.isCardOwnedByClient(cardNumber, clientId);

        // Assert
        assertTrue(result);
        verify(cardRepository).findByCardNumberAndClientId(cardNumber, clientId);
    }

    @Test
    void isCardOwnedByClient_ShouldReturnFalse_WhenCardDoesNotBelongToClient() {
        // Arrange
        String cardNumber = "4111111111111111";
        Long clientId = 999L;
        when(cardRepository.findByCardNumberAndClientId(cardNumber, clientId)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.isCardOwnedByClient(cardNumber, clientId);

        // Assert
        assertFalse(result);
        verify(cardRepository).findByCardNumberAndClientId(cardNumber, clientId);
    }
}