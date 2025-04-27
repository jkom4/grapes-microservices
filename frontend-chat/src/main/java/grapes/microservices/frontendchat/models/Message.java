package grapes.microservices.frontendchat.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Message(
        String id,
        String topicId,
        User sender,
        String content,
        LocalDateTime timestamp
){
    public static DateTimeFormatter humanReadableFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter preciseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public String getHumainReadableDate() {
        return this.timestamp().format(humanReadableFormat);
    }

    public String getDateToString() {
        return this.timestamp().format(preciseFormat);
    }
}
