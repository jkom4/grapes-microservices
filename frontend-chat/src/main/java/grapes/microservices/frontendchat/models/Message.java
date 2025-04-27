package grapes.microservices.frontendchat.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Message {
    String id;
    String topicId;
    User sender;
    String content;
    LocalDateTime timestamp;
    // Used to display the origin in the View (backend/multicast/pusher)
    private Origin origin = Origin.backend;

    public Message(String id, String topicId, User sender, String content, LocalDateTime timestamp) {
        this.id = id;
        this.topicId = topicId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static DateTimeFormatter humanReadableFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter preciseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public String getHumainReadableDate() {
        return this.getTimestamp().format(humanReadableFormat);
    }

    public String getDateToString() {
        return this.getTimestamp().format(preciseFormat);
    }

    public enum Origin {
        backend,
        pusher,
        multicast,
        local
    }
}
