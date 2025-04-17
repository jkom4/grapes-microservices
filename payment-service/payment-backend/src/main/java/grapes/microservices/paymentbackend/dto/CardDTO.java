package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for payment card information transfer between application layers.
 * Contains card details needed for payment processing and client card management.
 * Includes transient security fields not persisted to the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private Long clientId;
    private String cardNumber;
    private String maskedCardNumber;
    private String expirationDate;
    private String cardholderName;
    private String cardType;
    private String status;
    private LocalDate addedDate;

    // Transient security field - not stored in database
    private String cvv;
}