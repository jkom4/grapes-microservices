package grapes.microservices.authservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import grapes.microservices.authservice.dto.AuthEvent;
import grapes.microservices.authservice.dto.AuthEventPayload;
import grapes.microservices.authservice.dto.EventPayload;
import grapes.microservices.authservice.dto.RegistrationEventPayload;
import grapes.microservices.authservice.utils.AuthLogger;
import grapes.microservices.authservice.utils.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;


/**
 * Service responsible for sending authentication logs to a RabbitMQ queue.
 * This service produces authentication event messages and sends them to a configured RabbitMQ exchange/queue.
 */
@Service
public class AuthEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Value("${spring.application.name}")
    private String sourceSystem;

    /**
     * Sends an authentication log message to the RabbitMQ queue.
     * It creates an AuthEvent object and serializes it to JSON before sending it to the queue.
     *
     * @param payload the AuthEventPayload object containing the details of the authentication attempt.
     */
    public void sendAuthLog(AuthEventPayload payload) {
        sendMessage(payload, "AuthenticationAttempt", rabbitMQConfig.getAuthLogsQueue());
    }

    /**
     * Sends a registration log message to the RabbitMQ queue.
     * @param payload the RegistrationEventPayload object containing the details of the registration attempt.
     */
    public void sendRegistrationLog(RegistrationEventPayload payload) {
        sendMessage(payload, "RegistrationAttempt", rabbitMQConfig.getRegistrationLogsQueue());
    }

    /**
     * Sends a message to the RabbitMQ queue.
     * This method is used for sending messages to the RabbitMQ queue.
     *
     * @param payload the message to be sent
     */
    private void sendMessage(EventPayload payload, String eventType, String queue) {
        logger.info(" Message sent to RabbitMQ: " + payload);
        AuthEvent event = new AuthEvent(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now().toString(),
                sourceSystem,
                "1.0",
                payload
        );

        try {
            // Convert the event to a JSON string
            String json = objectMapper.writeValueAsString(event);

            // Send the JSON message to the RabbitMQ queue
            rabbitTemplate.convertAndSend(queue, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}