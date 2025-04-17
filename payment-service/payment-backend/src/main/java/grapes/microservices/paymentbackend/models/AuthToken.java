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

    /**
     * Unique token value used for authentication
     */
    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Indicates whether this token has already been used
     */
    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    /**
     * Client associated with this authentication token
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;

    /**
     * Constructor that initializes a new token with default values.
     * Sets expiration time to 3 minutes after creation.
     *
     * @param token The token string
     * @param client The client this token belongs to
     */
    public AuthToken(String token, Client client) {
        this.token = token;
        this.client = client;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(3); // Token valid for 3 minutes
        this.isUsed = false;
    }

    /**
     * Checks if this token is currently valid.
     * A token is valid if it hasn't been used and hasn't expired.
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }
}