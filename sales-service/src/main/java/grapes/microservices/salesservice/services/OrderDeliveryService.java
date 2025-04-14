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
     * @param tripId the delivery ORDER ID (trip ID)
     * @return a list of order items to be delivered
     */
    public List<OrderDTO> getOrdersForTrip(Integer tripId) {
        Integer orderId = deliveryRepository.findByOrderId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ORDER_ID: " + tripId))
                .getOrderId();

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return orderItems.stream()
                .map(item -> new OrderDTO(
                        item.getId(),
                        item.getArticle() != null ? item.getArticle().getName() : "Unknown product",
                        item.getQuantity() != null ? item.getQuantity() : item.getQuantityKg(),
                        tripId,
                        Boolean.TRUE.equals(item.getScanned())
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
        orderItem.setScanned(true);
        orderItem.setScannedAt(java.time.LocalDateTime.now());
        orderItemRepository.save(orderItem);
    }

    /**
     * Marks all order items of a trip as scanned.
     *
     * @param tripId the trip (delivery) ID
     */
    public void markAllOrderItemsAsScannedForTrip(Integer tripId) {
        Integer orderId = deliveryRepository.findByOrderId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ORDER_ID: " + tripId))
                .getOrderId();

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            item.setScanned(true);
            item.setScannedAt(java.time.LocalDateTime.now());
        }

        orderItemRepository.saveAll(orderItems);
    }
}
