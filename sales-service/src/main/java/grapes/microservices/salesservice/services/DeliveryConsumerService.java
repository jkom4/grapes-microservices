package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.models.Delivery;
import grapes.microservices.salesservice.models.DeliveryMessage;
import grapes.microservices.salesservice.models.DeliveryStatus;
import grapes.microservices.salesservice.repositories.DeliveryRepository;
import grapes.microservices.salesservice.repositories.DeliveryStatusRepository;
import grapes.microservices.salesservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryConsumerService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderRepository orderRepository;

    private static final Integer PENDING_STATUS_ID = 1;

    @RabbitListener(queues = "order-paid-queue")
    public void receiveOrder(DeliveryMessage message) {

        try {
            var order = orderRepository.findById(message.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + message.getOrderId()));

            Integer deliveryManId = findBestDeliveryManId();

            if (deliveryManId == null) {
                throw new IllegalStateException(" No available deliveryman found!");
            }

            DeliveryStatus pendingStatus = deliveryStatusRepository.findByLabel("Pending")
                    .orElseThrow(() -> new IllegalArgumentException("'Pending' delivery status not found"));

            Delivery delivery = Delivery.builder()
                    .orderId(message.getOrderId())
                    .userId(deliveryManId)
                    .deliveryStatusId(pendingStatus.getId())
                    .deliveryDate(LocalDateTime.now())
                    .name(message.getCustomerName())
                    .address(message.getAddress())
                    .phoneNumber(message.getPhoneNumber())
                    .build();

            deliveryRepository.save(delivery);


        } catch (Exception ex) {
            System.err.println(" Error while creating delivery: " + ex.getMessage());
        }
    }

    //  Find the delivery man who has the less "Pending" deliveries
    private Integer findBestDeliveryManId() {
        List<Integer> deliveryMenIds = List.of(2, 3, 4);

        Integer bestDeliveryManId = null;
        long minPendingDeliveries = Long.MAX_VALUE;

        for (Integer deliveryManId : deliveryMenIds) {
            long pendingDeliveries = deliveryRepository.countByUserIdAndDeliveryStatusId(deliveryManId, PENDING_STATUS_ID);

            if (pendingDeliveries < minPendingDeliveries) {
                minPendingDeliveries = pendingDeliveries;
                bestDeliveryManId = deliveryManId;
            }
        }

        return bestDeliveryManId;
    }
}
