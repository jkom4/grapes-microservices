package grapes.microservices.paymentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentBackendApplication.class, args);
		System.out.println("[INFO] Payment Backend Server running on http://127.0.0.1:8043/");
	}
}