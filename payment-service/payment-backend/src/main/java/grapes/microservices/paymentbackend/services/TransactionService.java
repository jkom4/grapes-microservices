package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.*;
import grapes.microservices.paymentbackend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import grapes.microservices.paymentbackend.config.RabbitMQConfig;
import grapes.microservices.paymentbackend.dto.PaymentValidatedMessageDTO;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import grapes.microservices.paymentbackend.repositories.CardRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Service responsible for managing payment transaction records in the system.
 * Handles creation, completion (by ID), and failure of transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;

    private final ClientRepository clientRepository;
    private final CardRepository cardRepository;
    private final RabbitTemplate rabbitTemplate;


    @Value("${app.grapes.account.number}")
    private String grapesAccountNumber;


    /**
     * Creates a new transaction record for a payment initiation.
     * The transaction is initially marked as "Initiated" / "Pending".
     *
     * @param paymentRequest DTO containing payment details (amount, merchant name)
     * @param client The client initiating the payment
     * @return The newly created and saved TransactionEntity
     */
    @Transactional
    public TransactionEntity createPaymentTransaction(PaymentRequestDTO paymentRequest, Client client) {
        log.info("Creating new payment transaction record for client ID: {}", client.getId());

        // 1. Get client's primary/default account
        Optional<Account> accountOpt = accountRepository.findFirstByClientIdOrderByOpeningDateDesc(client.getId());
        if (accountOpt.isEmpty()) {
            log.error("Cannot create transaction: No account found for client ID: {}", client.getId());
            throw new IllegalStateException("Client has no associated account to debit from.");
        }
        Account account = accountOpt.get();

        // 2. Get merchant info
        String merchantName = paymentRequest.getMerchantName() != null && !paymentRequest.getMerchantName().isEmpty() ?
                paymentRequest.getMerchantName() : "Grapes";
        Optional<Merchant> merchantOpt = merchantRepository.findByMerchantName(merchantName);
        String businessSector = merchantOpt.map(Merchant::getBusinessSector).orElse("Unknown");
        if (merchantOpt.isEmpty()) {
            log.warn("Merchant '{}' not found. Using default sector '{}'.", merchantName, businessSector);
        }

        // 3. Create new transaction using the entity's constructor
        TransactionEntity transaction = new TransactionEntity(
                account.getAccountNumber(), // Debtor account from client
                account.getBank() != null ? account.getBank().getBankName() : "Unknown Bank",
                client.getId(),
                account.getAccountNumber(), // Client account number explicit field
                paymentRequest.getAmount(),
                merchantName,
                businessSector
        );

        // 4. Save transaction in database
        TransactionEntity savedTransaction = transactionRepository.save(transaction);
        log.info("Created and saved transaction with ID: {} for client: {}", savedTransaction.getId(), client.getId());

        return savedTransaction;
    }

    /**
     * Completes a specific payment transaction identified by its ID after successful verification.
     * Updates the client's account balance, credits the Grapes account, and marks the transaction as "Completed".
     *
     * @param client The client whose payment is being completed (used for verification and balance check)
     * @param amount The amount of the transaction (retrieved from cache, used for verification)
     * @param transactionId The ID of the specific transaction to complete
     * @return The completed TransactionEntity
     * @throws IllegalStateException if transaction not found, state invalid, balance insufficient, or client/amount mismatch.
     * @throws SecurityException if client mismatch occurs.
     */
    @Transactional
    public TransactionEntity completePaymentTransaction(Client client, BigDecimal amount, Long transactionId,Long orderId) {
        log.info("Completing payment transaction ID: {}, client ID: {}, amount: {}", transactionId, client.getId(), amount);

        // Find and validate the transaction
        Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            log.error("Cannot complete transaction: Transaction not found with ID: {}", transactionId);
            throw new IllegalStateException("Transaction with ID " + transactionId + " not found for completion.");
        }
        TransactionEntity transaction = transactionOpt.get();

        validateTransactionForCompletion(transaction, client, amount);


        Account clientAccount = updateClientAccountBalance(transaction);


        Account grapesAccount = updateGrapesAccountBalance(amount);


        updateTransactionStatus(transaction, clientAccount.getBalance(), grapesAccount.getBalance());

        try {

            Account debtorAccount = accountRepository.findByAccountNumber(transaction.getClientAccountNumber()).orElse(null);

            List<Card> clientCards = cardRepository.findByClientId(client.getId());
            String cardType = null;
            if (clientCards != null && !clientCards.isEmpty()) {
                cardType = clientCards.get(0).getCardType();
            }


            Integer age = null;
            if (client.getBirthDate() != null) {
                age = Period.between(client.getBirthDate(), LocalDate.now()).getYears();
            }

            PaymentValidatedMessageDTO messageDTO = new PaymentValidatedMessageDTO(
                    orderId,
                    client.getFullName(),
                    client.getId(),
                    debtorAccount != null ? debtorAccount.getAccountNumber() : transaction.getClientAccountNumber(),
                    cardType,
                    client.getGender(),
                    client.getBirthDate(),
                    age,
                    client.getMaritalStatus(),
                    client.getAverageMonthlySalary(),
                    transaction.getId(),
                    transaction.getTransactionDateTime(),
                    debtorAccount != null && debtorAccount.getBank() != null ? debtorAccount.getBank().getBankName() : transaction.getDebtorBank(),
                    transaction.getTransferAmount()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_VALIDATED_QUEUE, messageDTO);
            log.info("Sent payment validation message to queue '{}' for transaction ID {}", RabbitMQConfig.PAYMENT_VALIDATED_QUEUE, transaction.getId());

        } catch (Exception e) {
            log.error("Failed to send payment validation message to RabbitMQ for transaction ID {}: {}", transactionId, e.getMessage(), e);
        }

        return transaction;
    }



    /**
     * Validates if a transaction can be completed
     */
    private void validateTransactionForCompletion(TransactionEntity transaction, Client client, BigDecimal amount) {
        if (!"Initiated".equals(transaction.getStatus())) {
            log.warn("Transaction {} is not in 'Initiated' state (current: {}). Cannot complete.",
                    transaction.getId(), transaction.getStatus());
            throw new IllegalStateException("Transaction " + transaction.getId() +
                    " is not in a completable state (" + transaction.getStatus() + ").");
        }

        if (!transaction.getClientId().equals(client.getId())) {
            log.error("Security Alert: Client ID {} attempting to complete transaction {} owned by client {}",
                    client.getId(), transaction.getId(), transaction.getClientId());
            throw new SecurityException("Client mismatch for the transaction completion.");
        }

        if (transaction.getTransferAmount().compareTo(amount) != 0) {
            log.error("Amount mismatch for transaction {}. Expected: {}, Received for completion: {}",
                    transaction.getId(), transaction.getTransferAmount(), amount);
            transaction.markAsFailed("Amount Mismatch");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Amount mismatch during transaction completion.");
        }
    }

    /**
     * Updates the client's account balance
     */
    private Account updateClientAccountBalance(TransactionEntity transaction) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(transaction.getClientAccountNumber());
        if (accountOpt.isEmpty()) {
            log.error("Cannot complete transaction {}: Client Account {} not found in DB!",
                    transaction.getId(), transaction.getClientAccountNumber());
            transaction.markAsFailed("Client Account Not Found");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Client account associated with the transaction not found.");
        }

        Account account = accountOpt.get();
        BigDecimal amount = transaction.getTransferAmount();

        // Check if balance is sufficient
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient balance for account {} to complete transaction {}. Required: {}, Available: {}.",
                    account.getAccountNumber(), transaction.getId(), amount, account.getBalance());
            transaction.markAsFailed("Insufficient Balance");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Insufficient account balance to complete the payment.");
        }

        // Update balance
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Debited account: {}. New balance: {}", account.getAccountNumber(), newBalance);

        return account;
    }

    /**
     * Updates the Grapes account balance
     */
    private Account updateGrapesAccountBalance(BigDecimal amount) {
        Optional<Account> grapesAccountOpt = accountRepository.findByAccountNumber(grapesAccountNumber);
        Account grapesAccount = grapesAccountOpt.orElseGet(() -> {
            log.warn("Grapes account {} not found! Creating it.", grapesAccountNumber);
            Account newGrapesAcc = new Account();
            newGrapesAcc.setAccountNumber(grapesAccountNumber);
            newGrapesAcc.setBalance(BigDecimal.ZERO);
            newGrapesAcc.setAccountType("Internal");
            newGrapesAcc.setStatus("Active");
            return newGrapesAcc;
        });

        BigDecimal currentGrapesBalance = grapesAccount.getBalance() != null ?
                grapesAccount.getBalance() : BigDecimal.ZERO;
        BigDecimal newGrapesBalance = currentGrapesBalance.add(amount);
        grapesAccount.setBalance(newGrapesBalance);
        accountRepository.save(grapesAccount);
        log.info("Credited Grapes account: {}. New balance: {}", grapesAccountNumber, newGrapesBalance);

        return grapesAccount;
    }

    /**
     * Updates the transaction status to completed
     */
    private void updateTransactionStatus(TransactionEntity transaction, BigDecimal clientNewBalance, BigDecimal grapesNewBalance) {
        transaction.markAsCompleted(clientNewBalance);
        transaction.setCreditorAccountNewBalance(grapesNewBalance);
        transaction.setTransactionDateTime(LocalDateTime.now());
        transactionRepository.save(transaction);
        log.info("Transaction ID {} successfully marked as completed.", transaction.getId());
    }

    /**
     * Marks a transaction as failed in the database.
     * @param transactionId The ID of the transaction to mark as failed
     * @param reason A short description of why it failed
     * @return The updated TransactionEntity
     * @throws IllegalStateException if the transaction is not found
     */
    @Transactional
    public TransactionEntity failTransaction(Long transactionId, String reason) {
        log.warn("Marking transaction ID {} as failed. Reason: {}", transactionId, reason);

        Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            log.error("Cannot fail transaction: Transaction not found with ID: {}", transactionId);
            return null;
        }

        TransactionEntity transaction = transactionOpt.get();

        // Check if already completed or failed to avoid unintended state changes
        if ("Completed".equals(transaction.getStatus()) || "Failed".equals(transaction.getStatus())) {
            log.warn("Transaction ID {} is already in final state '{}'. Not marking as failed again.",
                    transactionId, transaction.getStatus());
            return transaction;
        }

        // Mark transaction as failed
        transaction.markAsFailed(reason);

        // Save the updated transaction
        return transactionRepository.save(transaction);
    }

    /**
     * Finds a transaction by its ID. Used by PaymentController.
     * @param transactionId The ID to search for.
     * @return Optional containing the transaction if found.
     */
    public Optional<TransactionEntity> findTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }
}