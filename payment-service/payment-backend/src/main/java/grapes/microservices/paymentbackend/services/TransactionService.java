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
 * Handles creation, completion, and failure of transactions during the payment process.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;

    // Grapes bank account constant
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

        // 2. Get merchant info (or use default 'Grapes')
        String merchantName = paymentRequest.getMerchantName() != null && !paymentRequest.getMerchantName().isEmpty() ?
                paymentRequest.getMerchantName() : "Grapes"; // Default merchant name

        Optional<Merchant> merchantOpt = merchantRepository.findByMerchantName(merchantName);
        String businessSector = "Unknown"; // Default if merchant not found or has no sector
        if (merchantOpt.isPresent() && merchantOpt.get().getBusinessSector() != null) {
            businessSector = merchantOpt.get().getBusinessSector();
        } else {
            log.warn("Merchant '{}' not found or has no business sector defined. Using default '{}'.", merchantName, businessSector);
        }

        // 3. Create new transaction using the entity's constructor
        TransactionEntity transaction = new TransactionEntity(
                account.getAccountNumber(),
                account.getBank() != null ? account.getBank().getBankName() : "Unknown Bank",
                client.getId(),
                account.getAccountNumber(),
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
     * Completes a payment transaction after successful verification (e.g., OTP confirmed).
     * Updates the client's account balance, credits the Grapes account, and marks the transaction as "Completed".
     *
     * @param client The client whose payment is being completed
     * @param amount The amount of the transaction
     * @param paymentAttemptId Context ID for logging/tracing
     * @return The completed TransactionEntity
     * @throws IllegalStateException if client has no account or insufficient balance
     */
    @Transactional
    public TransactionEntity completePaymentTransaction(Client client, BigDecimal amount, String paymentAttemptId) {
        log.info("Completing payment transaction for client ID: {}, amount: {}, context ID: {}", client.getId(), amount, paymentAttemptId);

        // 1. Find client's primary account
        Optional<Account> accountOpt = accountRepository.findFirstByClientIdOrderByOpeningDateDesc(client.getId());
        if (accountOpt.isEmpty()) {
            log.error("Cannot complete transaction: No account found for client ID: {}", client.getId());
            throw new IllegalStateException("Client has no associated account to complete the payment.");
        }
        Account account = accountOpt.get();

        // 2. Check if account balance is sufficient
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient balance for client ID: {}. Required: {}, Available: {}. Aborting completion.",
                    client.getId(), amount, account.getBalance());
            throw new IllegalStateException("Insufficient account balance to complete the payment.");
        }

        // 3. Update account balance for the client (debtor)
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Updated balance for account: {}, new balance: {}", account.getAccountNumber(), newBalance);

        // 4. Get and update Grapes account (creditor)
        Optional<Account> grapesAccountOpt = accountRepository.findByAccountNumber(GRAPES_ACCOUNT_NUMBER);
        Account grapesAccount;

        if (grapesAccountOpt.isEmpty()) {
            log.warn("Grapes account not found in database. Creating a new one.");
            grapesAccount = new Account();
            grapesAccount.setAccountNumber(GRAPES_ACCOUNT_NUMBER);
            grapesAccount.setBalance(BigDecimal.ZERO);
        } else {
            grapesAccount = grapesAccountOpt.get();
        }

        // Update Grapes account balance
        BigDecimal newGrapesBalance = grapesAccount.getBalance().add(amount);
        grapesAccount.setBalance(newGrapesBalance);
        accountRepository.save(grapesAccount);
        log.info("Updated Grapes account balance: {}, new balance: {}", GRAPES_ACCOUNT_NUMBER, newGrapesBalance);

        // 5. Find the corresponding transaction initiated earlier
        TransactionEntity transaction = null;
        try {
            var pendingTransactions = transactionRepository.findByClientIdAndStatus(client.getId(), "Initiated");
            if (!pendingTransactions.isEmpty()) {
                transaction = pendingTransactions.stream()
                        .filter(t -> t.getTransferAmount().compareTo(amount) == 0)
                        .sorted((t1, t2) -> t2.getTransactionDateTime().compareTo(t1.getTransactionDateTime()))
                        .findFirst()
                        .orElse(null);

                if (transaction != null) {
                    log.info("Found existing transaction ID {} in 'Initiated' state for client {} and amount {}.",
                            transaction.getId(), client.getId(), amount);
                } else {
                    log.warn("Could not find a unique 'Initiated' transaction for client {} and amount {}. A new completed record might be created.",
                            client.getId(), amount);
                }
            }
        } catch (Exception e) {
            log.error("Error trying to find existing transaction for context {}: {}", paymentAttemptId, e.getMessage());
        }

        // If no existing transaction found, create a new one
        if (transaction == null) {
            log.warn("No suitable existing transaction found for context {}. Creating a new completed transaction record.", paymentAttemptId);
            transaction = new TransactionEntity(
                    account.getAccountNumber(),
                    account.getBank() != null ? account.getBank().getBankName() : "Unknown Bank",
                    client.getId(),
                    account.getAccountNumber(),
                    amount,
                    "Grapes",
                    "Unknown"
            );
            transaction.setId(null);
        }

        // 6. Update transaction status and balance details
        transaction.markAsCompleted(newBalance);
        transaction.setCreditorAccountNewBalance(newGrapesBalance);
        transaction.setTransactionDateTime(LocalDateTime.now());

        // 7. Save updated transaction
        TransactionEntity completedTransaction = transactionRepository.save(transaction);
        log.info("Transaction ID {} marked as completed.", completedTransaction.getId());

        return completedTransaction;
    }

    /**
     * Marks a transaction as failed in the database and restores any balances if needed.
     *
     * @param transactionId The ID of the transaction to mark as failed
     * @param reason A short description of why it failed
     * @return The updated TransactionEntity
     * @throws IllegalArgumentException if the transaction is not found
     */
    @Transactional
    public TransactionEntity failTransaction(Long transactionId, String reason) {
        log.warn("Marking transaction ID {} as failed. Reason: {}", transactionId, reason);

        Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            log.error("Cannot fail transaction: Transaction not found with ID: {}", transactionId);
            throw new IllegalArgumentException("Transaction not found with ID: " + transactionId);
        }

        TransactionEntity transaction = transactionOpt.get();

        // Check if already completed or failed
        if ("Completed".equals(transaction.getStatus()) || "Failed".equals(transaction.getStatus())) {
            log.warn("Transaction ID {} is already in state '{}'. Not marking as failed again.", transactionId, transaction.getStatus());
            return transaction;
        }

        // If transaction was in progress, restore Grapes account balance
        if ("Initiated".equals(transaction.getStatus()) && transaction.getTransferAmount() != null) {
            Optional<Account> grapesAccountOpt = accountRepository.findByAccountNumber(GRAPES_ACCOUNT_NUMBER);
            if (grapesAccountOpt.isPresent()) {
                Account grapesAccount = grapesAccountOpt.get();
                BigDecimal adjustedBalance = grapesAccount.getBalance().subtract(transaction.getTransferAmount());
                grapesAccount.setBalance(adjustedBalance);
                accountRepository.save(grapesAccount);
                log.info("Restored Grapes account balance after failed transaction. New balance: {}", adjustedBalance);
                transaction.setCreditorAccountNewBalance(adjustedBalance);
            } else {
                log.warn("Could not find Grapes account to restore balance after failed transaction!");
            }
        }

        // Mark transaction as failed
        transaction.markAsFailed(reason);

        return transactionRepository.save(transaction);
    }
}