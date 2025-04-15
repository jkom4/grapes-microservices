package grapes.microservices.salesservice;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class RabbitTestConfig {
    @Bean
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);  // 👈 un faux RabbitTemplate pour les tests
    }
}
