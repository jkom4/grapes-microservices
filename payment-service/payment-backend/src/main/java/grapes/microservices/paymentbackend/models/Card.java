package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing a payment card in the system.
 * Maps to the "cards" table in the database.
 */
@Data
@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    /**
     * Client who owns this card
     */
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Full card number
     */
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    /**
     * Card expiration date in MM/YY format
     */
    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    /**
     * Type of card (e.g., VISA, MasterCard)
     */
    @Column(name = "card_type")
    private String cardType;

    /**
     * Current status of the card (e.g., active, blocked, expired)
     */
    @Column(name = "card_status", nullable = false)
    private String status;

    @Column(name = "added_date", nullable = false)
    private LocalDate addedDate;

    /**
     * Constructor with essential fields that sets default values
     * for status and added date.
     *
     * @param client The client who owns this card
     * @param cardNumber The card's full number
     * @param expirationDate The card's expiration date
     * @param cardholderName The name of the cardholder
     */
    public Card(Client client, String cardNumber, String expirationDate, String cardholderName) {
        this.client = client;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cardholderName = cardholderName;
        this.status = "active"; // Default status
        this.addedDate = LocalDate.now();
    }

    /**
     * Returns a masked version of the card number for security,
     * showing only the last 4 digits.
     *
     * @return Masked card number (e.g., ************1234)
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }
}