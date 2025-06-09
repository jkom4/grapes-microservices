package grapes.microservices.paymentbackend.listeners;

import grapes.microservices.paymentbackend.config.RabbitMQConfig;
import grapes.microservices.paymentbackend.dto.AuthRegistrationMessageDTO;
import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Bank;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AccountRepository;
import grapes.microservices.paymentbackend.repositories.BankRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Component that listens for messages on the authentication registration queue
 * and creates new clients with associated banking data, using configured
 * default values for password and phone number.
 */
@Component
public class AuthRegistrationListener {

    private static final Logger log = LoggerFactory.getLogger(AuthRegistrationListener.class);
    private static final Random random = new Random();

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private BankRepository bankRepository;


    @Value("${app.grapes.client.PASSWORD}")
    private String clientPasswordHash;


    @Value("${app.grapes.client.PHONE}")
    private String clientPhoneNumber;


    /**
     * Listens for messages on the authentication registration queue.
     * Creates a new client and associated banking data if the client does not already exist.
     * Uses configured default password and phone number.
     *
     * @param message The message containing the new user's information.
     */
    @RabbitListener(queues = RabbitMQConfig.AUTH_REGISTRATION_QUEUE)
    @Transactional // Ensure all DB operations are in a single transaction
    public void receiveAuthRegistrationMessage(AuthRegistrationMessageDTO message) {
        log.info("Received registration message for email: {}", message.getEmail());

        try {
            // 1. Check if email already exists
            if (clientRepository.existsByEmail(message.getEmail())) {
                log.warn("Client with email {} already exists. Skipping registration.", message.getEmail());
                return;
            }

            // 2. Create the Client entity
            Client newClient = new Client();

            // Map fields from the message to the Client entity
            try {
                newClient.setId(Long.parseLong(message.getClient_id()));
            } catch (NumberFormatException e) {
                log.error("Invalid ID format received in message: {}. Cannot set client_id.", message.getClient_id());
                return;
            }
            newClient.setEmail(message.getEmail());
            newClient.setLastName(message.getName());
            newClient.setFirstName(message.getFirstName());
            newClient.setGender(message.getGender());
            newClient.setBirthDate(message.getBirth_date());
            newClient.setNationalRegistryNumber(message.getNational_id());
            newClient.setAddress(message.getAddressAsString());
            newClient.setPassword(clientPasswordHash);
            newClient.setPhoneNumber(clientPhoneNumber);
            newClient.setMaritalStatus(getRandomMaritalStatus());
            newClient.setAverageMonthlySalary(generateRandomSalary());
            newClient.setRegistrationDate(LocalDate.now());
            newClient.setStatus("Active");
            Client savedClient = clientRepository.save(newClient);
            log.info("Successfully created client with ID: {}", savedClient.getId());
            List<Bank> banks = bankRepository.findAll();
            if (banks.isEmpty()) {
                log.error("No banks found in the database. Cannot create account for client {}", savedClient.getId());
                throw new IllegalStateException("Bank data is missing.");
            }
            Bank selectedBank = banks.get(random.nextInt(banks.size()));

            Account newAccount = new Account();
            newAccount.setAccountNumber(generateUniqueAccountNumber());
            newAccount.setClient(savedClient);
            newAccount.setBank(selectedBank);
            newAccount.setAuthenticationType("3D Secure");
            newAccount.setBalance(generateRandomBalance());
            newAccount.setOpeningDate(LocalDate.now());
            newAccount.setAccountType("Current");
            newAccount.setStatus("Active");

            accountRepository.save(newAccount);
            log.info("Successfully created account {} for client {}", newAccount.getAccountNumber(), savedClient.getId());

            // 5. Create a fake and UNIQUE bank card
            Card newCard = new Card();
            newCard.setClient(savedClient);
            newCard.setCardNumber(generateUniqueCardNumber());
            newCard.setExpirationDate(generateFutureExpirationDate());
            newCard.setCardholderName(savedClient.getFullName());
            newCard.setCardType(random.nextBoolean() ? "Visa" : "Mastercard");
            newCard.setStatus("active");
            newCard.setAddedDate(LocalDate.now());

            cardRepository.save(newCard);
            log.info("Successfully created {} card {} for client {}", newCard.getCardType(), newCard.getMaskedCardNumber(), savedClient.getId());

        } catch (Exception e) {
            log.error("Failed to process registration message for email {}: {}", message.getEmail(), e.getMessage(), e);

        }
    }

    // --- Utility methods for generating fake data ---
    private String getRandomMaritalStatus() {
        String[] statuses = {"Single", "Married", "Divorced", "Widowed"};
        return statuses[random.nextInt(statuses.length)];
    }
    private BigDecimal generateRandomSalary() {
        double salary = 2000 + (8000 * random.nextDouble());
        return BigDecimal.valueOf(salary).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    private BigDecimal generateRandomBalance() {
        double balance = 500 + (10000 * random.nextDouble());
        return BigDecimal.valueOf(balance).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = ThreadLocalRandom.current().nextLong(1_000_000_000_000L);
            accountNumber = String.format("BE%02d%012d", random.nextInt(99), number);
        } while (accountRepository.existsById(accountNumber));
        return accountNumber;
    }

    /**
     * Generates a unique and valid card number according to the Luhn algorithm.
     * Always produces a 16-digit card number (standard format).
     *
     * @return A valid and unique card number
     */
    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            // Start with 4 (Visa prefix) for simplicity
            StringBuilder sb = new StringBuilder("4");

            // Generate 14 random digits (the 16th will be the check digit)
            for (int i = 0; i < 14; i++) {
                sb.append(random.nextInt(10));
            }

            // Calculate and add the check digit for the Luhn algorithm
            String partialNumber = sb.toString();
            int checkDigit = calculateLuhnCheckDigit(partialNumber);
            sb.append(checkDigit);

            cardNumber = sb.toString();
        } while (cardRepository.findByCardNumber(cardNumber).isPresent());

        return cardNumber;
    }

    /**
     * Calculates the check digit that would make a card number valid according to the Luhn algorithm.
     *
     * @param partialNumber The card number without the last check digit
     * @return The check digit that would make the number valid
     */
    private int calculateLuhnCheckDigit(String partialNumber) {
        int sum = 0;
        boolean alternate = true;

        // Process from right to left
        for (int i = partialNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(partialNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        // The check digit is what we need to add to make the sum divisible by 10
        int remainder = sum % 10;
        return remainder == 0 ? 0 : 10 - remainder;
    }
    private String generateFutureExpirationDate() {
        YearMonth current = YearMonth.now();
        YearMonth expiry = current.plusYears(3 + random.nextInt(3)).plusMonths(random.nextInt(12));
        return expiry.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }
}