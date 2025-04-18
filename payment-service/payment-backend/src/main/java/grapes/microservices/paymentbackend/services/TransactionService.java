package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.*;
import grapes.microservices.paymentbackend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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

    private static final String GRAPES_ACCOUNT_NUMBER = "BE15203672485394";

    /**
     * Creates a new transaction record for a payment initiation.
     * The transaction is initially marked as "Initiated" / "Pending".
     *
     * @param paymentRequest DTO containing payment details (amount, merchant name)
     * @param client The client initiating the payment
     * @return The newly created and saved TransactionEntity
     * @throws IllegalStateException if the client has no associated account
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
    public TransactionEntity completePaymentTransaction(Client client, BigDecimal amount, Long transactionId) {
        log.info("Completing payment transaction ID: {}, client ID: {}, amount: {}", transactionId, client.getId(), amount);

        // 1. Find THE specific transaction by ID
        Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            log.error("Cannot complete transaction: Transaction not found with ID: {}", transactionId);
            throw new IllegalStateException("Transaction with ID " + transactionId + " not found for completion.");
        }
        TransactionEntity transaction = transactionOpt.get();

        // 2. Verify current state and ownership
        if (!"Initiated".equals(transaction.getStatus())) {
            log.warn("Transaction {} is not in 'Initiated' state (current: {}). Cannot complete.",
                    transactionId, transaction.getStatus());
            throw new IllegalStateException("Transaction "+ transactionId + " is not in a completable state (" + transaction.getStatus() + ").");
        }
        if (!transaction.getClientId().equals(client.getId())) {
            log.error("Security Alert: Client ID {} attempting to complete transaction {} owned by client {}",
                    client.getId(), transactionId, transaction.getClientId());
            throw new SecurityException("Client mismatch for the transaction completion.");
        }
        if (transaction.getTransferAmount().compareTo(amount) != 0) {
            log.error("Amount mismatch for transaction {}. Expected: {}, Received for completion: {}",
                    transactionId, transaction.getTransferAmount(), amount);
            // Mark as failed? Or just throw an exception? Let's throw an exception.
            transaction.markAsFailed("Amount Mismatch");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Amount mismatch during transaction completion.");
        }

        // 3. Find client's account (should exist as it was checked during initiation)
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(transaction.getClientAccountNumber());
        if (accountOpt.isEmpty()) {
            log.error("Cannot complete transaction {}: Client Account {} not found in DB!", transactionId, transaction.getClientAccountNumber());
            transaction.markAsFailed("Client Account Not Found");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Client account associated with the transaction not found.");
        }
        Account account = accountOpt.get();

        // 4. Check and update client balance
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient balance for client ID: {} / Account {} to complete transaction {}. Required: {}, Available: {}.",
                    client.getId(), account.getAccountNumber(), transactionId, amount, account.getBalance());
            transaction.markAsFailed("Insufficient Balance");
            transactionRepository.save(transaction);
            throw new IllegalStateException("Insufficient account balance to complete the payment.");
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Debited account: {}. New balance: {}", account.getAccountNumber(), newBalance);

        // 5. Get and update Grapes account (creditor)
        Optional<Account> grapesAccountOpt = accountRepository.findByAccountNumber(GRAPES_ACCOUNT_NUMBER);
        Account grapesAccount = grapesAccountOpt.orElseGet(() -> {
            log.warn("Grapes account {} not found! Creating it.", GRAPES_ACCOUNT_NUMBER);
            Account newGrapesAcc = new Account();
            newGrapesAcc.setAccountNumber(GRAPES_ACCOUNT_NUMBER);
            newGrapesAcc.setBalance(BigDecimal.ZERO);
            newGrapesAcc.setAccountType("Internal");
            newGrapesAcc.setStatus("Active");

            return newGrapesAcc;
        });
        BigDecimal currentGrapesBalance = grapesAccount.getBalance() != null ? grapesAccount.getBalance() : BigDecimal.ZERO;
        BigDecimal newGrapesBalance = currentGrapesBalance.add(amount);
        grapesAccount.setBalance(newGrapesBalance);
        accountRepository.save(grapesAccount);
        log.info("Credited Grapes account: {}. New balance: {}", GRAPES_ACCOUNT_NUMBER, newGrapesBalance);

        // 6. Update THE transaction found by ID
        transaction.markAsCompleted(newBalance); // Sets status, status3DS, debtor balance
        transaction.setCreditorAccountNewBalance(newGrapesBalance);
        transaction.setTransactionDateTime(LocalDateTime.now());

        // 7. Save updated transaction
        TransactionEntity completedTransaction = transactionRepository.save(transaction);
        log.info("Transaction ID {} successfully marked as completed.", completedTransaction.getId());

        return completedTransaction;
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
            // Not throwing an exception here allows the caller to continue if trying to fail a non-existent transaction
            return null;
        }

        TransactionEntity transaction = transactionOpt.get();

        // Check if already completed or failed to avoid unintended state changes
        if ("Completed".equals(transaction.getStatus()) || "Failed".equals(transaction.getStatus())) {
            log.warn("Transaction ID {} is already in final state '{}'. Not marking as failed again.", transactionId, transaction.getStatus());
            return transaction; // Return current state
        }

        // Mark transaction as failed - The markAsFailed method updates the statuses
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