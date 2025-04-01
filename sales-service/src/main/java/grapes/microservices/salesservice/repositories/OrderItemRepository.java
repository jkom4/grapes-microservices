package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByCartId(Integer cartId);
    void deleteByCartIdAndId(Integer cartId, Integer id);
    List<OrderItem> findByOrderId(Integer orderId);

}
