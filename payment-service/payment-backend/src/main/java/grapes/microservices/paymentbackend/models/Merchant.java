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


    @Id
    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "business_sector")
    private String businessSector;

    @Column(name = "merchant_address")
    private String merchantAddress;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate;


    @Column(name = "merchant_status")
    private String merchantStatus;
}