package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Bank entity operations.
 */
@Repository
public interface BankRepository extends JpaRepository<Bank, String> {
    /**
     * Finds a bank by its name.
     *
     * @param bankName the name of the bank
     * @return an Optional containing the Bank entity if found, or empty if not found
     */
    Optional<Bank> findByBankNameIgnoreCase(String bankName);
}