package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.ActivityLogEvent;
import grapes.microservices.salesservice.dto.PaymentValidatedMessageDTO;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.Transaction;
import grapes.microservices.salesservice.repositories.OrderRepository;
import grapes.microservices.salesservice.repositories.TransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Autowired
    public PaymentEventConsumer(RabbitTemplate rabbitTemplate,
                                TransactionRepository transactionRepository,
                                OrderRepository orderRepository,
                                OrderService orderService) {
        this.rabbitTemplate = rabbitTemplate;
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @RabbitListener(queues = "payment-validated-queue")
    public void onPaymentValidated(PaymentValidatedMessageDTO message) throws FileNotFoundException {
        System.out.println("[Sales-Service] Received payment validated message: " + message);

        String localTransactionId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .transactionId(localTransactionId)
                .orderId(message.getOrderId())
                .userId(message.getClientId())
                .bankTransactionId(String.valueOf(message.getTransactionId()))
                .paymentMethod(message.getCardType())
                .amount(message.getTransferAmount())
                .currency(message.getCurrency())
                .paymentStatus(message.getPaymentStatus())
                .deliveryStatus(message.getDeliveryStatus())
                .deliveryTimeDays(message.getDeliveryTimeDays())
                .transactionDateTime(message.getTransactionDateTime())
                .build();

        transactionRepository.save(transaction);
        System.out.println("[Sales-Service] Transaction saved in DB with UUID: " + localTransactionId);

        Order order = orderRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + message.getOrderId()));
        order.setPaid(true);
        orderRepository.save(order);
        System.out.println("[Sales-Service] Order ID " + order.getId() + " marked as paid.");

        orderService.finalizePaymentAndClearCart(
                order.getId(),
                message.getAddress(),
                message.getPhoneNumber(),
                message.getCustomerName()
        );

        ActivityLogEvent activityLog = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TransactionCompleted")
                .eventTimestamp(LocalDateTime.now().toString())
                .sourceSystem("SalesService")
                .version("1.1")
                .payload(ActivityLogEvent.Payload.builder()
                        .sourceTransactionId("BANK_TX_" + message.getTransactionId())
                        .clientId("user_" + message.getClientName())
                        .productId("PROD_FRUIT_003") // TODO: Dynamique plus tard
                        .serviceId(null)
                        .transactionTimestamp(message.getTransactionDateTime().toString())
                        .quantity(1) // TODO: récup réelle quantité
                        .unitPrice(message.getTransferAmount())
                        .totalAmount(message.getTransferAmount())
                        .currency(message.getCurrency())
                        .paymentMethod(message.getCardType())
                        .paymentStatus("Success")
                        .deliveryStatus("Pending")
                        .deliveryTimeDays(message.getDeliveryTimeDays())
                        .build())
                .build();

        rabbitTemplate.convertAndSend("q_activity_logs", activityLog);
        System.out.println("[Sales-Service] Activity log sent to 'q_activity_logs': " + activityLog);
    }
}
