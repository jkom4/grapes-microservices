package grapes.microservices.chatservice.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    private String chatId;
    private String senderId;
    private String content;
    private LocalDateTime createdAt;
}
