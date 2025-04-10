package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus, Integer> {
    Optional<DeliveryStatus> findByLabel(String label);
}
