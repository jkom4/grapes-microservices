package grapes.microservices.salesservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_QUEUE = "orderQueue";
    public static final String ORDER_PAID_QUEUE = "order-paid-queue";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }

    @Bean
    public Queue salesDataQueue() {
        return new Queue("sales-data-queue", true);
    }

    @Bean
    public Queue paymentValidatedQueue() {
        return new Queue("payment-validated-queue", true);
    }

    @Bean
    public Queue activityLogsQueue() {
        return new Queue("q_activity_logs", true);
    }


    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
