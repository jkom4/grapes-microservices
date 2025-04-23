package grapes.microservices.paymentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class PaymentBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentBackendApplication.class, args);
		log.info("Payment Backend Server running on http://127.0.0.1:8093/");
		log.info("Payment Backend Swagger on http://localhost:8093/swagger-ui/index.html");

	}
}