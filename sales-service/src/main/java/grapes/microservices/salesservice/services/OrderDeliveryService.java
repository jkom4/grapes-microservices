package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Retrieves all order items associated with a specific trip (delivery).
     *
     * @param tripId the delivery ID (trip ID)
     * @return a list of order items to be delivered
     */
    public List<OrderDTO> getOrdersForTrip(Integer tripId) {
        Integer orderId = deliveryRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"))
                .getOrderId();

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return orderItems.stream()
                .map(item -> new OrderDTO(
                        item.getId(),
                        "Product description to fetch", // ( To be updated later by linking with the Article entity)
                        item.getQuantity() != null ? item.getQuantity() : item.getQuantityKg(),
                        tripId,
                        Boolean.TRUE.equals(item.getScanned()) //  use getScanned() (Boolean) safely
                ))
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific order item as scanned (delivered).
     *
     * @param orderItemId the ID of the order item to update
     */
    public void markOrderItemAsScanned(Integer orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found"));

        orderItem.setScanned(true); //  mark as scanned
        orderItem.setScannedAt(java.time.LocalDateTime.now()); //  update scanned timestamp

        orderItemRepository.save(orderItem);
    }
}
