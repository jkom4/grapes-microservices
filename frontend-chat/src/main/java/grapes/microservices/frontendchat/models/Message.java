package grapes.microservices.frontendchat.models;

import java.time.LocalDateTime;

public record Message(
        int topicId,
        User sender,
        String content,
        LocalDateTime timestamp
){

}
