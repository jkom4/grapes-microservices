package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.ActivityLogEvent;
import grapes.microservices.salesservice.dto.PaymentValidatedMessageDTO;
import grapes.microservices.salesservice.models.Transaction;
import grapes.microservices.salesservice.repositories.TransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionRepository transactionRepository;

    @Autowired
    public PaymentEventConsumer(RabbitTemplate rabbitTemplate, TransactionRepository transactionRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.transactionRepository = transactionRepository;
    }

    @RabbitListener(queues = "payment-validated-queue")
    public void onPaymentValidated(PaymentValidatedMessageDTO message) {
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

        ActivityLogEvent activityLog = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TransactionCompleted")
                .eventTimestamp(LocalDateTime.now().toString())
                .sourceSystem("SalesService")
                .version("1.1")
                .payload(ActivityLogEvent.Payload.builder()
                        .sourceTransactionId("BANK_TX_" + message.getTransactionId())
                        .clientId("user_" + message.getClientName())
                        .productId("PROD_FRUIT_003") // TODO : mettre à jour selon l’article concerné
                        .serviceId(null)
                        .transactionTimestamp(message.getTransactionDateTime().toString())
                        .quantity(1) // TODO : mettre à jour si tu récupères la vraie quantité
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
