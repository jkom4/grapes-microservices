package grapes.microservices.paymentbackend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a bank in the payment system.
 * Maps to the "bank" table in the database.
 */
@Data
@Entity
@Table(name = "bank")
@NoArgsConstructor
@AllArgsConstructor
public class Bank {

    /**
     * The bank's name serves as the primary key
     */
    @Id
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_address")
    private String bankAddress;

    /**
     * Country where the bank is located
     */
    @Column(name = "country")
    private String country;
}