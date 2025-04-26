package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    boolean existsByCode(Integer code);
    List<Order> findByUserId(String userId);


}
