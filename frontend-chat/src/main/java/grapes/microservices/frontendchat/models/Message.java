package grapes.microservices.frontendchat.models;

import java.time.LocalDateTime;

public record Message(
        User sender,
        String content,
        LocalDateTime timestamp,
        String topicId
){}
