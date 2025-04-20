package grapes.microservices.chatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "chat-exchange";
    public static final String QUEUE_PREFIX = "chat.queue.";
    public static final String ROUTING_KEY_PREFIX = "chat.to.room.";

    @Bean
    public Exchange chatExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME)
                .durable(true)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(QUEUE_PREFIX + "default", true);
    }

    @Bean
    public Binding bindingTopic(Queue chatQueue, Exchange chatExchange) {
        return BindingBuilder.bind(chatQueue)
                .to(chatExchange)
                .with(ROUTING_KEY_PREFIX + "*")
                .noargs();
    }
}