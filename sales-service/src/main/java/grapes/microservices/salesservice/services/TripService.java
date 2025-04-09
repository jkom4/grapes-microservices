package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.dto.TripDTO;
import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    //  1. recuperate all the trips of 1 deliver
    public List<TripDTO> getTripsByDeliveryMan(Integer userId) {
        List<Delivery> deliveries = deliveryRepository.findByUserId(userId);

        return deliveries.stream()
                .map(delivery -> new TripDTO(
                        delivery.getId(),                            // tripId = deliveryId
                        "Trip " + delivery.getId(),                  // name of  trip
                        "Unknown distance",                         // Distance (placeholder)
                        "Address placeholder",                      // Adresses (placeholder)
                        delivery.getDeliveryStatusId() != null && delivery.getDeliveryStatusId() == 3
                ))
                .collect(Collectors.toList());
    }

    //  2. recuperate all the products for a trip
    public List<OrderDTO> getOrdersForTrip(Integer tripId) {
        Delivery delivery = deliveryRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

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

    //  3. mark a product is scanned
    public void updateScanStatus(Integer orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found with ID: " + orderItemId));
        orderItem.setScanned(true);
        orderItemRepository.save(orderItem);
    }

    //  4. mark a trip like done
    public void finishTrip(Integer tripId) {
        Delivery delivery = deliveryRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        delivery.setDeliveryStatusId(3);
        delivery.setDeliveredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
    }
}
