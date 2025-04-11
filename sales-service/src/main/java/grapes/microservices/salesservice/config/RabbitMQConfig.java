package grapes.microservices.salesservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_QUEUE = "orderQueue";
    public static final String ORDER_PAID_QUEUE = "order-paid-queue";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true); // true is for create a new Queue
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true); // true is for create a new queue
    }
}
