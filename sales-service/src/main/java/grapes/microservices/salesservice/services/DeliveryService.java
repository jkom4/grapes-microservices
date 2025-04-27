package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.DeliveryDTO;
import grapes.microservices.salesservice.dto.DeliveryFeedbackDTO;
import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.DeliveryStatus;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.DeliveryStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;

    // View pending deliveries
    public List<DeliveryDTO> getPendingDeliveries() {
        DeliveryStatus pendingStatus = deliveryStatusRepository.findByLabel("Pending")
                .orElseThrow(() -> new IllegalArgumentException("Status 'Pending' not found"));

        List<Delivery> pendingDeliveries = deliveryRepository.findByDeliveryStatusId(pendingStatus.getId());

        return pendingDeliveries.stream()
                .map(delivery -> DeliveryDTO.builder()
                        .id(delivery.getId())
                        .orderId(delivery.getOrderId())
                        .deliveryDate(delivery.getDeliveryDate())
                        .statusLabel(pendingStatus.getLabel())
                        .build())
                .collect(Collectors.toList());
    }

    // Get client-facing delivery status (preparing, shipped, delivered)
    public String getDeliveryStatusByOrderId(Integer orderId) {
        Delivery delivery = deliveryRepository.findAll()
                .stream()
                .filter(d -> d.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for order ID: " + orderId));

        DeliveryStatus status = deliveryStatusRepository.findById(delivery.getDeliveryStatusId())
                .orElseThrow(() -> new IllegalArgumentException("Delivery status not found"));

        // Map driver status ➔ client status
        return switch (status.getLabel()) {
            case "Pending" -> "Preparing";    // Driver Pending → Client Preparing
            case "In Progress" -> "Shipped";   // Driver In Progress → Client Shipped
            case "Delivered" -> "Delivered";   // Driver Delivered → Client Delivered
            default -> "Unknown Status";       // Safety
        };
    }

    public void updateDeliveryStatus(Integer orderId, String newStatusLabel) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for order ID: " + orderId));

        DeliveryStatus newStatus = deliveryStatusRepository.findByLabel(newStatusLabel)
                .orElseThrow(() -> new IllegalArgumentException("Delivery status '" + newStatusLabel + "' not found"));

        delivery.setDeliveryStatusId(newStatus.getId());
        deliveryRepository.save(delivery);
    }

    public void saveDeliveryFeedback(DeliveryFeedbackDTO feedback) {
        System.out.println("Saving feedback for orderId: " + feedback.getOrderId());
        System.out.println("Signature length: " + (feedback.getSignature() != null ? feedback.getSignature().length : 0));
        Delivery delivery = new Delivery();
        delivery.setOrderId(feedback.getOrderId());
        delivery.setComment(feedback.getComment());
        delivery.setDoorstep(feedback.isDoorstep());
        delivery.setSignature(feedback.getSignature());
        delivery.setDeliveryStatusId(feedback.getDeliveryStatusId());
        deliveryRepository.save(delivery);
    }

    public void updateDeliveryFeedback(Integer orderId, DeliveryFeedbackDTO feedback) {
        System.out.println("Updating feedback for orderId: " + orderId);
        System.out.println("Signature length: " + (feedback.getSignature() != null ? feedback.getSignature().length : 0));
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid orderId: " + orderId);
        }
        if (feedback.isDoorstep() && feedback.getSignature() == null) {
            throw new IllegalArgumentException("Signature required for doorstep delivery");
        }
        Optional<Delivery> existingDelivery = deliveryRepository.findByOrderId(orderId);
        if (existingDelivery.isPresent()) {
            Delivery delivery = existingDelivery.get();
            delivery.setComment(feedback.getComment());
            delivery.setDoorstep(feedback.isDoorstep());
            delivery.setSignature(feedback.getSignature());
            delivery.setDeliveryStatusId(feedback.getDeliveryStatusId());
            deliveryRepository.save(delivery);
        } else {
            throw new IllegalArgumentException("No delivery found for orderId: " + orderId);
        }
    }
}