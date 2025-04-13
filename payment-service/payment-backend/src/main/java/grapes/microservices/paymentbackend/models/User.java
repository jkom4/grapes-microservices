package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_expiration")
    private String cardExpiration;

    @Column(name = "account_balance")
    private Double accountBalance;


    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(String login, String password, String phoneNumber) {
        this.login = login;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
}

