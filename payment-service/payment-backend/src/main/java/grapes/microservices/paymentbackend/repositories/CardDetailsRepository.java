package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.CardDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardDetailsRepository extends JpaRepository<CardDetails, Long> {
    Optional<CardDetails> findByCardNumberAndUserId(String cardNumber, Long userId);

    // Ajout de cette méthode pour rechercher par numéro de carte uniquement
    Optional<CardDetails> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);
}