package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);
    Optional<AuthToken> findByTokenAndUser(String token, User user);
    Optional<AuthToken> findFirstByUserOrderByCreatedAtDesc(User user);
}