package grapes.microservices.chatservice.config;

import grapes.microservices.chatservice.dto.MessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @RabbitListener(queues = "#{@chatQueue.name}")
    public void receiveMessage(MessageDto message) {
        System.out.println("Received from the broker : " + message);
    }
}

