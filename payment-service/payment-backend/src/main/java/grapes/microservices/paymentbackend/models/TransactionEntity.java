package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * Default accounts used for Grapes bank internal transfers
     */
    private static final String GRAPES_ACCOUNT = "BE15203672485394"; // Bank of Grapes account
    private static final String GRAPES_BANK = "Bank of Grapes";

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

    /**
     * Type of transaction (e.g., Payment, Transfer, Withdrawal)
     */
    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "client_account_number")
    private String clientAccountNumber;

    /**
     * Timestamp when the transaction was initiated
     */
    @Column(name = "transaction_date_time")
    private LocalDateTime transactionDateTime;

    /**
     * Amount being transferred in the transaction
     */
    @Column(name = "transfer_amount")
    private BigDecimal transferAmount;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_business_sector")
    private String merchantBusinessSector;

    /**
     * Type of 3D Secure authentication used (e.g., OTP, biometric)
     */
    @Column(name = "authentication_type_3ds")
    private String authenticationType3DS;

    /**
     * Status of the 3D Secure authentication process
     */
    @Column(name = "status_3ds")
    private String status3DS;

    /**
     * Updated balance of the debtor account after transaction
     */
    @Column(name = "debtor_account_new_balance")
    private BigDecimal debtorAccountNewBalance;

    /**
     * Updated balance of the creditor account after transaction
     */
    @Column(name = "creditor_account_new_balance")
    private BigDecimal creditorAccountNewBalance;

    /**
     * Overall transaction status (e.g., Initiated, Completed, Failed)
     */
    @Column(name = "status")
    private String status;

    // Used for generating unique transaction IDs
    private static final Random random = new Random();

    /**
     * Constructor for creating a new payment transaction.
     * Sets default values for authentication and transaction status.
     * Uses the Grapes account as the default creditor.
     *
     * @param debtorAccount Client's account to be debited
     * @param debtorBank Bank of the debtor account
     * @param clientId ID of the client making the transaction
     * @param clientAccountNumber Account number of the client
     * @param amount Amount to be transferred
     * @param merchantName Name of the merchant receiving payment
     * @param merchantBusinessSector Business sector of the merchant
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
        this.status3DS = "Pending";
        this.status = "Initiated";

        // Default values for creditor (Bank of Grapes)
        this.creditorAccount = GRAPES_ACCOUNT;
        this.creditorBank = GRAPES_BANK;

        // Creditor account balance will be updated in the service layer
    }

    /**
     * Updates transaction status to completed and sets the new debtor balance.
     *
     * @param debtorNewBalance Updated balance of the debtor account
     */
    public void markAsCompleted(BigDecimal debtorNewBalance) {
        this.status3DS = "Validated";
        this.status = "Completed";
        this.debtorAccountNewBalance = debtorNewBalance;
    }

    /**
     * Updates transaction status to failed.
     * Amount refund will be handled by the service layer.
     *
     * @param reason Reason for the transaction failure
     */
    public void markAsFailed(String reason) {
        this.status3DS = "Failed";
        this.status = "Failed";
    }

    /**
     * Generates a unique transaction ID based on current timestamp
     * plus a random number to avoid collisions.
     *
     * @return Unique transaction ID
     */
    private Long generateTransactionId() {
        return System.currentTimeMillis() + random.nextInt(1000);
    }
}