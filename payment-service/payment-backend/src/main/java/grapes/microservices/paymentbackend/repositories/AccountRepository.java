package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.Account;
import grapes.microservices.paymentbackend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByClient(Client client);

    List<Account> findByClientId(Long clientId);

    Optional<Account> findFirstByClientOrderByOpeningDateDesc(Client client);

    /**
     * Finds the most recently opened account for a client identified by ID.
     * Uses explicit JPQL query for more control over the ordering.
     */
    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId ORDER BY a.openingDate DESC")
    Optional<Account> findFirstByClientIdOrderByOpeningDateDesc(@Param("clientId") Long clientId);

    Optional<Account> findByAccountNumber(String grapesAccountNumber);
}