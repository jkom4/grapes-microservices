package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing a merchant in the payment system.
 * Maps to the "merchant" table in the database.
 */
@Data
@Entity
@Table(name = "merchant")
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    /**
     * Merchant name serves as the primary key identifier
     */
    @Id
    @Column(name = "merchant_name")
    private String merchantName;

    /**
     * Business sector or industry category
     */
    @Column(name = "business_sector")
    private String businessSector;

    @Column(name = "merchant_address")
    private String merchantAddress;

    /**
     * VAT registration number or tax identifier
     */
    @Column(name = "vat_number")
    private String vatNumber;

    /**
     * Date when the merchant was registered in the system
     */
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    /**
     * Current status of the merchant (e.g., active, suspended)
     */
    @Column(name = "merchant_status")
    private String merchantStatus;
}