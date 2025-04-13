package grapes.microservices.frontendchat.models.dto;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    int topicId;
    int senderId;
    String senderUsername;
    String content;
    String timestamp;

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static MessageDTO toDTO(Message msg) {
        return new MessageDTO(
                msg.topicId(),
                msg.sender().id(),
                msg.sender().username(),
                msg.content(),
                LocalDateTime.now().format(formatter));
    }

    public static Message toEntity(MessageDTO dto) {
        return new Message(
                dto.topicId,
                new User(dto.senderId, dto.senderUsername), // You need to provide a User instance here
                dto.content,
                LocalDateTime.parse(dto.timestamp, formatter)
        );
    }
}
