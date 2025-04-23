package grapes.microservices.paymentbackend.listeners;

import grapes.microservices.paymentbackend.config.RabbitMQConfig;
import grapes.microservices.paymentbackend.dto.PaymentValidatedMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidatedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentValidatedListener.class);

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_VALIDATED_QUEUE)
    public void receivePaymentValidatedMessage(PaymentValidatedMessageDTO message) {

        log.info("Received message from queue '{}':", RabbitMQConfig.PAYMENT_VALIDATED_QUEUE);

        log.info("Message Content: {}", message.toString());


        log.info("  Client ID: {}", message.getIdClient());
        log.info("  Client Name: {}", message.getNomClient());
        log.info("  Account Number: {}", message.getNumeroCompte());
        log.info("  Amount Transferred: {}", message.getSommeTransferee());


    }
}