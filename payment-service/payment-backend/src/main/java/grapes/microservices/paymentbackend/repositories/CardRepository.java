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
    /**
     * Finds all cards owned by a specific client
     */
    List<Card> findByClient(Client client);

    /**
     * Finds all cards owned by a client identified by ID
     */
    List<Card> findByClientId(Long clientId);

    /**
     * Finds a card by its card number
     */
    Optional<Card> findByCardNumber(String cardNumber);

    /**
     * Finds a card by its number and owner's ID
     * Used to verify card ownership during payment processing
     */
    Optional<Card> findByCardNumberAndClientId(String cardNumber, Long clientId);
}