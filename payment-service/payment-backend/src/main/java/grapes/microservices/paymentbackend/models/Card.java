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


    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;


    @Column(name = "card_number", nullable = false)
    private String cardNumber;


    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;


    @Column(name = "card_type")
    private String cardType;


    @Column(name = "card_status", nullable = false)
    private String status;

    @Column(name = "added_date", nullable = false)
    private LocalDate addedDate;


    public Card(Client client, String cardNumber, String expirationDate, String cardholderName) {
        this.client = client;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cardholderName = cardholderName;
        this.status = "active"; // Default status
        this.addedDate = LocalDate.now();
    }


    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }
}