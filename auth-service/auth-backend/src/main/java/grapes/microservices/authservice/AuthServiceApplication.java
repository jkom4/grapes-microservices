package grapes.microservices.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AuthServiceApplication.class, args);

        //byte[] aesKey = EncryptionService.generateAESKey();
        //System.out.println("Generated AES key: " + Base64.getEncoder().encodeToString(aesKey));
    }
}
