package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Transaction;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final AcsService acsService;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            UserService userService,
            TokenService tokenService,
            AcsService acsService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.acsService = acsService;
    }

    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    @Transactional
    public Transaction initiateTransaction(User user, Double amount, String merchant) {
        // Check if user has sufficient funds
        if (user.getAccountBalance() < amount) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setMerchant(merchant);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            return transactionRepository.save(transaction);
        }

        // Create a pending transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.INITIATED);

        // Save transaction
        transaction = transactionRepository.save(transaction);

        // Create authentication token and associate it with the transaction
        AuthToken token = tokenService.generateToken(user);
        transaction.setAuthCode(token.getToken());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Save updated transaction
        return transactionRepository.save(transaction);
    }

    @Transactional
    public boolean completeTransaction(String tokenValue) {
        // Verify the token
        if (!tokenService.verifyToken(tokenValue)) {
            return false;
        }

        // Find the transaction
        Optional<Transaction> transactionOpt = transactionRepository.findByAuthCode(tokenValue);
        if (transactionOpt.isEmpty()) {
            return false;
        }

        Transaction transaction = transactionOpt.get();
        User user = transaction.getUser();

        // Check if user has sufficient funds
        if (!userService.updateBalance(user, transaction.getAmount())) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return false;
        }

        // Send to ACS for processing
        boolean acsResponse = acsService.processPayment(transaction);

        if (acsResponse) {
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            return true;
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return false;
        }
    }
}