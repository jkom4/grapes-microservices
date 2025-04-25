package grapes.microservices.paymentbackend.config;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Defines the queue for payment validation.
     * @return Queue Bean.
     */
    public static final String PAYMENT_VALIDATED_QUEUE = "payment-validated-queue";
    @Bean
    public Queue paymentValidatedQueue() {

        return new Queue(PAYMENT_VALIDATED_QUEUE, true);
    }

    public static final String AUTH_REGISTRATION_QUEUE = "q_auth_registration";

    /**
     * Defines the queue for user registration messages.
     * @return Queue Bean.
     */
    @Bean
    public Queue authRegistrationQueue() {

        return new Queue(AUTH_REGISTRATION_QUEUE, true);

    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}