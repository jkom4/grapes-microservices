package grapes.microservices.frontendchat.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Message(
        String id,
        int topicId,
        User sender,
        String content,
        LocalDateTime timestamp
){
    public static DateTimeFormatter humanReadableFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String getDateToString() {
        return this.timestamp().format(humanReadableFormat);
    }
}
