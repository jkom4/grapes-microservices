package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TransactionEntity operations.
 * Provides methods to query transaction records in the database.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByClientId(Long clientId);
    List<TransactionEntity> findByMerchantName(String merchantName);


    List<TransactionEntity> findByTransactionDateTimeBetween(LocalDateTime start, LocalDateTime end);


    List<TransactionEntity> findByClientIdAndStatus(Long clientId, String status);
}