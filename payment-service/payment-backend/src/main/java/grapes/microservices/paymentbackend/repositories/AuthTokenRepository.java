package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for authentication token operations.
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> findByTokenAndClient(String token, Client client);

    Optional<AuthToken> findFirstByClientOrderByCreatedAtDesc(Client client);

    List<AuthToken> findByClientIdAndIsUsedFalseAndExpiresAtAfter(Long clientId, LocalDateTime now);
}