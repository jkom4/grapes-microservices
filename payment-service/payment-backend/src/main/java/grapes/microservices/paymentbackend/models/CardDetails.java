// --- START OF payment-backend/src/main/java/grapes/microservices/paymentbackend/models/CardDetails.java ---
package grapes.microservices.paymentbackend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data // Gère getters, setters, toString, equals, hashCode
@Entity
@Table(name = "transaction") // Assurez-vous que 'transaction' est la bonne table pour les détails de carte
@NoArgsConstructor // Nécessaire pour JPA
public class CardDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction") // Correspond à votre schéma Liquibase pour la clé primaire
    private Long id;

    @Column(name = "message") // Stocke la version masquée/JSON ou autre info
    private String cardDetails;

    @Column(name = "id_client", nullable = false)
    private Long userId;

    // --- MODIFICATIONS ---
    // Retrait de @Transient et ajout de @Column pour la persistance
    @Column(name = "card_number_persistent", nullable = true) // Rendre nullable=false si requis par la logique métier
    private String cardNumber;

    // Garder les autres comme Transient s'ils ne sont pas en BDD,
    // ou ajouter @Column si vous voulez les persister.
    @Transient
    private String expirationDate;

    @Transient
    private String cvv;

    // RETIRÉ : Le nom n'est plus géré ici directement
    // @Transient
    // private String cardholderName;
    // --- FIN MODIFICATIONS ---


    // Constructeur modifié (sans cardholderName, utilise User pour le nom)
    public CardDetails(String cardNumber, String expirationDate, String cvv, User user) {
        // Assigner aux champs (y compris le persistant cardNumber)
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cvv = cvv;

        if (user != null) {
            this.userId = user.getId();
            // Utilise le login de l'utilisateur comme nom récupéré pour l'affichage/log si besoin
            String userNameFromUserObject = user.getLogin();

            // Mise à jour de la chaîne JSON (sans cardholderName explicite, utilise le nom de l'user)
            this.cardDetails = String.format(
                    "{\"cardNumber\":\"%s\",\"expirationDate\":\"%s\",\"retrievedUserName\":\"%s\"}",
                    maskCardNumber(cardNumber), expirationDate, userNameFromUserObject
            );
        } else {
            this.userId = null; // Ou gérer l'erreur
            this.cardDetails = String.format( // Version sans nom utilisateur
                    "{\"cardNumber\":\"%s\",\"expirationDate\":\"%s\"}",
                    maskCardNumber(cardNumber), expirationDate
            );
            System.err.println("Warning: Creating CardDetails with a null User object.");
        }
    }

    // Méthode pour masquer le numéro de carte
    private String maskCardNumber(String number) {
        if (number == null || number.length() < 16) {
            return number != null ? number : "****"; // Gère null
        }
        // Masque tout sauf les 4 derniers chiffres
        return "XXXXXXXXXXXX" + number.substring(number.length() - 4);
    }

    // Les Getters et Setters sont générés par @Data
}
// --- END OF payment-backend/src/main/java/grapes/microservices/paymentbackend/models/CardDetails.java ---