package grapes.microservices.paymentbackend.models;

import lombok.AllArgsConstructor; // Vous pouvez la laisser si vous l'utilisez vraiment
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "transaction") // Assurez-vous que c'est la bonne table
public class CardDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long id;

    @Column(name = "message")
    private String cardDetails; // Stocke la version masquée/JSON

    @Column(name = "id_client", nullable = false)
    private Long userId; // Utilisé par findByCardNumberAndUserId

    // !! MODIFICATION CRUCIALE : Retirer @Transient et mapper à une colonne !!
    @Column(name = "card_number_persistent") // Utilisez un nom de colonne approprié
    private String cardNumber; // Ce champ doit être persistant pour la requête

    // Vous devrez peut-être aussi rendre ces champs persistants si nécessaire
    // Si vous les laissez @Transient, ils ne seront pas sauvés/lus de la DB
    @Transient // ou @Column(name="...") si vous voulez le persister
    private String expirationDate;

    @Transient // ou @Column(name="...") si vous voulez le persister
    private String cvv;

    @Transient // ou @Column(name="...") si vous voulez le persister
    private String cardholderName;

    // Constructeur par défaut
    public CardDetails() {}

    // Constructeur avec paramètres
    public CardDetails(String cardNumber, String expirationDate, String cvv, String cardholderName, User user) {
        // Assigner la valeur au champ persistant cardNumber
        this.cardNumber = cardNumber;

        // Initialiser les champs transients si vous les gardez
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.cardholderName = cardholderName;

        if (user != null) {
            this.userId = user.getId();
        }

        // Mise à jour de la chaîne JSON (peut-être redondante maintenant)
        this.cardDetails = String.format(
                "{\"cardNumber\":\"%s\",\"expirationDate\":\"%s\",\"cardholderName\":\"%s\"}",
                maskCardNumber(cardNumber), expirationDate, cardholderName
        );
    }

    // Méthode pour masquer le numéro de carte
    private String maskCardNumber(String number) {
        if (number == null || number.length() < 16) {
            return number;
        }
        return "XXXXXXXXXXXX" + number.substring(number.length() - 4);
    }

    // Getters et Setters gérés par @Data
}