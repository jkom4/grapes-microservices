package grapes.microservices.paymentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class PaymentBackendApplication {

	//@Value("${rabbitmq.queue.name}")
	//private String queueName;

	//@Value("${rabbitmq.exchange.name}")
	//private String exchangeName;

	////@Value("${rabbitmq.routing.key}")
	//private String routingKey;


	public static void main(String[] args) {
		SpringApplication.run(PaymentBackendApplication.class, args);
		System.out.println("[INFO] Payment Backend Server running on http://127.0.0.1:8043/");
	}

	//@Bean
	//public Queue queue() {
	//return new Queue(queueName, false);
	//}

	/*///@Bean
	public TopicExchange exchange() {
		return new TopicExchange(exchangeName);
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		return new RabbitTemplate(connectionFactory);
	}/*/
}