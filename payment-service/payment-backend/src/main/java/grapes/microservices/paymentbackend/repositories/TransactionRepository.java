package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.Transaction;
import grapes.microservices.paymentbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    Optional<Transaction> findByAuthCode(String authCode);
}