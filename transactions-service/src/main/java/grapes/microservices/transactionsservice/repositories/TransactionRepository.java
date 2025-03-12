package grapes.microservices.transactionsservice.repositories;


import grapes.microservices.transactionsservice.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByClientId(String clientId);
}
