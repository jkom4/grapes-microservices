package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.config.SalesDataMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SalesDataTestConsumer {

    @RabbitListener(queues = "sales-data-queue")
    public void receiveSalesData(SalesDataMessage message) {
        System.out.println(" [DataMining TEST] Sales data received:");
        System.out.println("→ Order ID: " + message.getOrderId());
        System.out.println("→ User ID: " + message.getUserId());
        System.out.println("→ Total Price: " + message.getTotalPrice());
        System.out.println("→ Created At: " + message.getCreatedAt());

        message.getItems().forEach(item ->
                System.out.println("    • ArticleID: " + item.getArticleId()
                        + ", Price: " + item.getPrice()
                        + ", Qty: " + item.getQuantity())
        );
    }
}