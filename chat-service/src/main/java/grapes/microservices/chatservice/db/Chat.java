package grapes.microservices.chatservice.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "chats")
public class Chat {
    @Id
    private String id;

    private String topic;

    private List<Participant> participants;

    @Data
    public static class Participant {
        private String userId;
    }
}
