package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByDeliveryStatusId(Integer deliveryStatusId);
    Optional<Delivery> findByOrderId(Integer orderId);

    List<Delivery> findByUserId(Integer userId);





}
