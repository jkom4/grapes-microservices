package grapes.microservices.paymentbackend.repositories;

import grapes.microservices.paymentbackend.models.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Merchant entity operations.
 * This interface extends JpaRepository to provide CRUD operations for the Merchant entity.
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {

    Optional<Merchant> findByMerchantName(String merchantName);
}