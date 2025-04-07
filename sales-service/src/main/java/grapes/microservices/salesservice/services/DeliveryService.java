package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.DeliveryDTO;
import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.DeliveryStatus;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.DeliveryStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, DeliveryStatusRepository deliveryStatusRepository) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryStatusRepository = deliveryStatusRepository;
    }

    public List<DeliveryDTO> getPendingDeliveries() {
        // Find the 'Pending' delivery status
        DeliveryStatus pendingStatus = deliveryStatusRepository.findByLabel("Pending")
                .orElseThrow(() -> new IllegalArgumentException(" Status 'Pending' not found"));

        // Fetch all deliveries with status 'Pending'
        List<Delivery> pendingDeliveries = deliveryRepository.findByDeliveryStatusId(pendingStatus.getId());

        // Map to DTOs
        return pendingDeliveries.stream()
                .map(delivery -> DeliveryDTO.builder()
                        .id(delivery.getId())
                        .orderId(delivery.getOrderId())
                        .deliveryDate(delivery.getDeliveryDate())
                        .statusLabel(pendingStatus.getLabel())
                        .build())
                .collect(Collectors.toList());
    }
}
