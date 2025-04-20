package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity representing authentication tokens used for client verification.
 * Maps to the "auth_tokens" table in the database.
 */
@Data
@Entity
@Table(name = "auth_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;


    @Column(name = "is_used", nullable = false)
    private boolean isUsed;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;


    public AuthToken(String token, Client client) {
        this.token = token;
        this.client = client;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(3);
        this.isUsed = false;
    }

    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }
}