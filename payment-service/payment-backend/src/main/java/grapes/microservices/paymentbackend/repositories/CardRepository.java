package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity operations.
 * Provides methods to query and manage payment cards in the database.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByClient(Client client);

    List<Card> findByClientId(Long clientId);

    Optional<Card> findByCardNumber(String cardNumber);

    Optional<Card> findByCardNumberAndClientId(String cardNumber, Long clientId);
}