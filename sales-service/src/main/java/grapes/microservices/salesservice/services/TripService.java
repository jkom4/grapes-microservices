package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.dto.TripDTO;
import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    // 1. Get all the trips from one delivery man
    public List<TripDTO> getTripsByDeliveryMan(Integer userId) {
        List<Delivery> deliveries = deliveryRepository.findByUserId(userId);

        return deliveries.stream()
                .map(delivery -> new TripDTO(
                        delivery.getOrderId(),
                        "Trip " + delivery.getOrderId(),
                        "Unknown distance",
                        delivery.getAddress(),
                        delivery.isFinished()
                ))
                .collect(Collectors.toList());
    }

    // 2. Get all products from a trip
    public List<OrderDTO> getOrdersForTrip(Integer tripId) {
        Delivery delivery = deliveryRepository.findByOrderId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ORDER_ID: " + tripId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(delivery.getOrderId());

        return orderItems.stream().map(orderItem -> {
            OrderDTO dto = new OrderDTO();
            dto.setOrderItemId(orderItem.getId());
            dto.setProductDescription(orderItem.getArticle().getName());
            dto.setQuantity(orderItem.getQuantityKg() != null ? orderItem.getQuantityKg() : orderItem.getQuantity());
            dto.setTripId(tripId);
            dto.setScanned(orderItem.getScanned() != null ? orderItem.getScanned() : false);
            return dto;
        }).collect(Collectors.toList());
    }

    // 3. Mark a product as scanned
    public void updateScanStatus(Integer orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found with ID: " + orderItemId));
        orderItem.setScanned(true);
        orderItemRepository.save(orderItem);
    }

    // 4. Finish a trip
    @Transactional
    public void finishTrip(Integer tripId) {
        Delivery delivery = deliveryRepository.findByOrderId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(delivery.getOrderId());

        boolean allScanned = orderItems.stream()
                .allMatch(item -> Boolean.TRUE.equals(item.getScanned()));

        if (!allScanned) {
            throw new IllegalStateException("Cannot finish trip: Some order items are not scanned.");
        }

        delivery.setDeliveryStatusId(3); // 3 = Finished
        delivery.setDeliveredAt(LocalDateTime.now());
        delivery.setFinished(true);
        System.out.println("Mise à jour de is_finished à true pour tripId: " + tripId);
        deliveryRepository.save(delivery);
        System.out.println("Delivery après sauvegarde: " + delivery);
    }

    // 5. Check if a trip exists
    public boolean existsTrip(Integer tripId) {
        return deliveryRepository.findByOrderId(tripId).isPresent();
    }
}