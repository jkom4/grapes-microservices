package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.ClientDTO;
import grapes.microservices.paymentbackend.mappers.UserMapper;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import grapes.microservices.paymentbackend.utils.PasswordManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * Service handling client-related operations including authentication,
 * account management, and financial operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final PasswordManager passwordManager;

    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }


    public boolean verifyCredentials(String email, String password) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);

        if (clientOpt.isEmpty()) {
            log.warn("Client not found with email: {}", email);
            return false;
        }

        Client client = clientOpt.get();
        boolean matches = passwordManager.matches(password, client.getPassword());
        log.info("Password verification for client {}: {}", email, matches ? "success" : "failed");
        return matches;
    }


    public List<Card> getClientCards(Long clientId) {
        return cardRepository.findByClientId(clientId);
    }


    public List<Account> getClientAccounts(Long clientId) {
        return accountRepository.findByClientId(clientId);
    }

    /**
     * Creates a new client from DTO data.
     * Handles password hashing and setting default values.
     *
     * @param clientDTO The client data transfer object
     * @return The created client entity
     * @throws NoSuchAlgorithmException If password hashing fails
     * @throws IllegalArgumentException If email already exists
     */
    @Transactional
    public Client createClient(ClientDTO clientDTO) throws NoSuchAlgorithmException {
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            log.warn("Client already exists with email: {}", clientDTO.getEmail());
            throw new IllegalArgumentException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        // Salt and hash password using PasswordManager
        String saltedPassword = passwordManager.saltPassword(clientDTO.getPassword());
        String hashedPassword = passwordManager.hashPassword(saltedPassword);

        // Create and save client entity using mapper
        Client client = UserMapper.toEntity(clientDTO);
        client.setPassword(hashedPassword);
        client.setStatus("Active");
        client.setRegistrationDate(java.time.LocalDate.now());

        log.info("Creating new client with email: {}", clientDTO.getEmail());
        return clientRepository.save(client);
    }

    /**
     * Updates a client's account balance by subtracting a specified amount.
     * Uses the most recent account for the client.
     *
     * @param clientId The client's ID
     * @param amountToSubtract The amount to subtract from the account balance
     * @return true if update successful, false if client/account not found or insufficient balance
     */
    @Transactional
    public boolean updateAccountBalance(Long clientId, BigDecimal amountToSubtract) {
        // 1. Get the client
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.error("Cannot update balance: Client not found with ID: {}", clientId);
            return false;
        }
        Client client = clientOpt.get();

        // 2. Find the client's primary/most recent account
        Optional<Account> accountOpt = accountRepository.findFirstByClientIdOrderByOpeningDateDesc(clientId);
        if (accountOpt.isEmpty()) {
            log.error("Cannot update balance: No accounts found for client ID: {}", clientId);
            return false;
        }
        Account account = accountOpt.get();

        // 3. Check if balance is sufficient
        if (account.getBalance() == null || account.getBalance().compareTo(amountToSubtract) < 0) {
            log.warn("Insufficient balance for client ID: {}. Required: {}, Available: {}",
                    clientId, amountToSubtract, account.getBalance());
            return false;
        }

        // 4. Update the balance
        BigDecimal newBalance = account.getBalance().subtract(amountToSubtract);
        account.setBalance(newBalance);
        accountRepository.save(account);

        log.info("Updated balance for client ID: {}, account: {}, new balance: {}",
                clientId, account.getAccountNumber(), newBalance);
        return true;
    }


    public boolean isCardOwnedByClient(String cardNumber, Long clientId) {
        return cardRepository.findByCardNumberAndClientId(cardNumber, clientId).isPresent();
    }
}