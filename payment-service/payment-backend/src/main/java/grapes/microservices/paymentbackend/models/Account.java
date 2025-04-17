package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a bank account in the system.
 * Maps to the "account" table in the database.
 */
@Data
@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    /**
     * The client who owns this account.
     */
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    /**
     * The bank where this account is held.
     */
    @ManyToOne
    @JoinColumn(name = "bank_name")
    private Bank bank;

    @Column(name = "authentication_type")
    private String authenticationType;

    /**
     * Current balance of the account.
     */
    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    /**
     * Type of account (e.g., savings, checking)
     */
    @Column(name = "account_type")
    private String accountType;

    /**
     * Current status of the account (e.g., active, suspended, closed)
     */
    @Column(name = "account_status")
    private String status;
}