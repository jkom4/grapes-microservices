package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Entity representing a financial transaction in the payment system.
 * Maps to the "transaction" table in the database.
 */
@Data
@Entity
@Table(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {


    @Id
    @Column(name = "transaction_id")
    private Long id;  // Manually generated ID

    @Column(name = "debtor_account")
    private String debtorAccount;

    @Column(name = "creditor_account")
    private String creditorAccount;

    @Column(name = "debtor_bank")
    private String debtorBank;

    @Column(name = "creditor_bank")
    private String creditorBank;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "client_account_number")
    private String clientAccountNumber;

    @Column(name = "transaction_date_time")
    private LocalDateTime transactionDateTime;

    @Column(name = "transfer_amount")
    private BigDecimal transferAmount;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_business_sector")
    private String merchantBusinessSector;

    @Column(name = "authentication_type_3ds")
    private String authenticationType3DS;

    @Column(name = "debtor_account_new_balance")
    private BigDecimal debtorAccountNewBalance;

    @Column(name = "creditor_account_new_balance")
    private BigDecimal creditorAccountNewBalance;

    @Column(name = "status")
    private String status;

    // Used for generating unique transaction IDs
    private static final Random random = new Random();

    /**
     * Constructor for creating a new payment transaction.
     * Sets default values for authentication and transaction status.
     * Uses the Grapes account as the default creditor.
     */
    public TransactionEntity(String debtorAccount, String debtorBank, Long clientId,
                             String clientAccountNumber, BigDecimal amount,
                             String merchantName, String merchantBusinessSector) {
        this.id = generateTransactionId();
        this.debtorAccount = clientAccountNumber;
        this.debtorBank = debtorBank;
        this.clientId = clientId;
        this.clientAccountNumber = clientAccountNumber;
        this.transactionType = "Payment";
        this.transactionDateTime = LocalDateTime.now();
        this.transferAmount = amount;
        this.merchantName = merchantName;
        this.merchantBusinessSector = merchantBusinessSector;
        this.authenticationType3DS = "OTP";
        this.status = "Initiated";
        this.creditorAccount = "BE15203672485394";
        this.creditorBank = "Grapes's bank";

    }


    public void markAsCompleted(BigDecimal debtorNewBalance) {
        this.status = "Completed";
        this.debtorAccountNewBalance = debtorNewBalance;
    }


    public void markAsFailed(String reason) {
        this.status = "Failed";
    }

    private Long generateTransactionId() {
        return System.currentTimeMillis() + random.nextInt(1000);
    }
}