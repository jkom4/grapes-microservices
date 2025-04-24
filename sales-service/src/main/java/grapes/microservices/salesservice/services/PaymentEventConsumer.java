package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.ActivityLogEvent;
import grapes.microservices.salesservice.dto.PaymentValidatedMessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PaymentEventConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "payment-validated-queue")
    public void onPaymentValidated(PaymentValidatedMessageDTO message) {
        System.out.println("[Sales-Service] Received payment validated message: " + message);

        // Convert to activity log format
        ActivityLogEvent activityLog = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TransactionCompleted")
                .eventTimestamp(LocalDateTime.now().toString())
                .sourceSystem("SalesService")
                .version("1.1")
                .payload(ActivityLogEvent.Payload.builder()
                        .sourceTransactionId("BANK_TX_" + message.getTransactionId())
                        .clientId("user_" + message.getClientName())
                        .productId("PROD_FRUIT_003")
                        .serviceId(null)
                        .transactionTimestamp(message.getTransactionDateTime().toString())
                        .quantity(1)
                        .unitPrice(message.getTransferAmount())
                        .totalAmount(message.getTransferAmount())
                        .currency("EUR")
                        .paymentMethod(message.getCardType())
                        .paymentStatus("Success")
                        .deliveryStatus("Pending")
                        .deliveryTimeDays(2)
                        .build())
                .build();

        rabbitTemplate.convertAndSend("q_activity_logs", activityLog);
        System.out.println("[Sales-Service] Sent activity log to q_activity_logs: " + activityLog);
    }
}

