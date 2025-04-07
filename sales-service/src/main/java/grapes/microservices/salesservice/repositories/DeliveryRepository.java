package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByDeliveryStatusId(Integer deliveryStatusId);

}
