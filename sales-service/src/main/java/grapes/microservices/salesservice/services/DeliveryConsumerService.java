package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.DeliveryStatus;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.DeliveryStatusRepository;
import grapes.microservices.salesservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryConsumerService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderRepository orderRepository;

    @RabbitListener(queues = "order-paid-queue")
    public void receiveOrder(Integer orderId) {
        System.out.println("Message received from RabbitMQ: Order ID = " + orderId);

        try {
            // 1. Check if the order exists
            orderRepository.findById(orderId).orElseThrow(() ->
                    new IllegalArgumentException("Order not found with ID: " + orderId)
            );

            // 2. (Future feature) In a future sprint, automatically assign an available delivery driver

            // 3. Retrieve the "Pending" delivery status
            DeliveryStatus pendingStatus = deliveryStatusRepository.findByLabel("Pending")
                    .orElseThrow(() -> new IllegalArgumentException("'Pending' delivery status not found"));

            // 4. Create new delivery
            Delivery delivery = Delivery.builder()
                    .orderId(orderId)
                    .deliveryStatusId(pendingStatus.getId())
                    .deliveryDate(LocalDateTime.now())
                    // .userId(deliveryManId)  // Later: assign delivery driver
                    .build();

            // 5. Save delivery
            deliveryRepository.save(delivery);

            System.out.println("Delivery automatically created for order ID: " + orderId);

        } catch (IllegalArgumentException ex) {
            System.err.println("Error while processing delivery for order ID: " + orderId);
            System.err.println("Details: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Unexpected error while creating delivery for order ID: " + orderId);
            ex.printStackTrace();
        }
    }

    // (Optional) Generate a tracking URL
    private String generateTrackingUrl(Integer orderId) {
        return "https://grapes.delivery/track/" + orderId;
    }
}
