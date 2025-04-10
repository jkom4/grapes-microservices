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
import java.util.List;
import java.util.Random;

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
            var order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

            // 🛠️ 1. recuperate the random delivery man
            Integer deliveryManId = findRandomDeliveryManId();

            if (deliveryManId == null) {
                throw new IllegalStateException("No available deliveryman found!");
            }

            DeliveryStatus pendingStatus = deliveryStatusRepository.findByLabel("Pending")
                    .orElseThrow(() -> new IllegalArgumentException("'Pending' delivery status not found"));

            // create delivery
            Delivery delivery = Delivery.builder()
                    .orderId(orderId)
                    .userId(deliveryManId)
                    .deliveryStatusId(pendingStatus.getId())
                    .deliveryDate(LocalDateTime.now())
                    .build();

            deliveryRepository.save(delivery);

            System.out.println("Delivery automatically created for order ID: " + orderId + ", assigned to deliveryman ID: " + deliveryManId);

        } catch (Exception ex) {
            System.err.println("Error while creating delivery: " + ex.getMessage());
        }
    }

    private Integer findRandomDeliveryManId() {
        List<Integer> deliveryMenIds = List.of(2, 3, 4);

        if (deliveryMenIds.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(deliveryMenIds.size());
        return deliveryMenIds.get(randomIndex);
    }




    // (Optional) Generate a tracking URL
    private String generateTrackingUrl(Integer orderId) {
        return "https://grapes.delivery/track/" + orderId;
    }
}
