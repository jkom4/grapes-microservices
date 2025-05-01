package grapes.microservices.frontendchat.models.dto;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {
    public static DateTimeFormatter preciseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static MessageDTO toDTO(Message msg) {

        return new MessageDTO(
                msg.getId(),
                msg.getTopicId(),
                msg.getSender().id(),
                msg.getSender().username(),
                msg.getContent(),
                msg.getTimestamp().format(preciseFormat)
        );
    }

    public static Message toEntity(MessageDTO dto) {
        return new Message(
                dto.getId(),
                dto.getTopicId(),
                new User(dto.getUserId(), dto.getUsername()), // Create a User instance
                dto.getContent(),
                LocalDateTime.parse(dto.getCreatedAt())
        );
    }

    public static List<Message> toEntityList(List<MessageDTO> dtoList) {
        return dtoList.stream()
                .map(MessageMapper::toEntity)
                .collect(Collectors.toList());
    }
}
