package grapes.microservices.authservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import grapes.microservices.authservice.dto.AuthEvent;
import grapes.microservices.authservice.dto.AuthEventPayload;
import grapes.microservices.authservice.utils.AuthLogger;
import grapes.microservices.authservice.utils.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class AuthEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Value("${spring.application.name}")
    private String sourceSystem;

    public AuthEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendAuthLog(AuthEventPayload payload) {
        logger.info(" Message sent to RabbitMQ: " + payload);
        AuthEvent event = new AuthEvent(
                payload.auth_attempt_id(),
                "AuthenticationAttempt",
                Instant.now().toString(),
                sourceSystem,
                "1.0",
                payload
        );

        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(rabbitMQConfig.getAuthLogsQueue(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}