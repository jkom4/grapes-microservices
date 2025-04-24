package grapes.microservices.frontendchat.models.dto;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {
    public static DateTimeFormatter preciseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");

    public static MessageDTO toDTO(Message msg) {
        return new MessageDTO(
                msg.id(),
                msg.topicId() + "",
                msg.sender().id(),
                msg.sender().username(),
                msg.content(),
                LocalDateTime.now().format(preciseFormat)
        );
    }

    public static Message toEntity(MessageDTO dto) {
        return new Message(
                dto.getId(),
                Integer.parseInt(dto.getTopicId()),
                new User(dto.getUserId(), dto.getUsername()), // Create a User instance
                dto.getContent(),
                LocalDateTime.parse(dto.getCreatedAt(), preciseFormat)
        );
    }

    public static List<Message> toEntityList(List<MessageDTO> dtoList) {
        return dtoList.stream()
                .map(MessageMapper::toEntity)
                .collect(Collectors.toList());
    }
}
