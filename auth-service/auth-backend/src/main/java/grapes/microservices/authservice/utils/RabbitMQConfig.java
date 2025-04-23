package grapes.microservices.authservice.utils;

import lombok.Data;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Data
@Configuration
public class RabbitMQConfig {

    private final String authLogsQueue;

    private final String registrationLogsQueue;

    public RabbitMQConfig(@Value("${rabbit.mq.queue.name}") String authLogsQueue, @Value("${rabbit.mq.queue.registration.name}") String registrationLogsQueue) {
        this.authLogsQueue = authLogsQueue;
        this.registrationLogsQueue = registrationLogsQueue;
    }

    @Bean
    public Queue authQueue() {
        return new Queue(authLogsQueue, true);
    }

    @Bean
    public Queue registrationQueue() {
        return new Queue(registrationLogsQueue, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}