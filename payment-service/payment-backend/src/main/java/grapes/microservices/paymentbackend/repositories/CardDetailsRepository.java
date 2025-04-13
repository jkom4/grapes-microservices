package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.CardDetails;
// Retirez l'import de User s'il n'est plus utilisé ailleurs dans ce fichier
// import grapes.microservices.paymentbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardDetailsRepository extends JpaRepository<CardDetails, Long> {


    Optional<CardDetails> findByCardNumberAndUserId(String cardNumber, Long userId); // Nouvelle méthode

    boolean existsByCardNumber(String cardNumber); // Garder celle-ci si elle existe déjà
}