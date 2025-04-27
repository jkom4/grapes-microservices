package grapes.microservices.chatservice.services;

import grapes.microservices.chatservice.dto.ActivityLogEvent;
import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.models.Message;
import grapes.microservices.chatservice.repositories.ChatRepository;
import grapes.microservices.chatservice.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class TopicService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    public static DateTimeFormatter preciseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final RabbitTemplate rabbitTemplate;

    public List<TopicDto> getAllTopics() {
        return chatRepository.findAll()
                .stream()
                .map(chat -> TopicDto.builder()
                        .id(chat.getId())
                        .topic(chat.getTopic())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MessageDto> getMessagesByTopicId(String topicId) {
        return messageRepository.findByChatId(topicId)
                .stream()
                .map(message -> MessageDto.builder()
                        .id(message.getId())
                        .userId(message.getSenderId())
                        .username(message.getUsername())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt().toString())
                        .topicId(message.getChatId())
                        .build())
                .collect(Collectors.toList());
    }

    public MessageDto postMessage(MessageDto dto) {
        Message message = Message.builder()
                .chatId(dto.getTopicId())
                .senderId(dto.getUserId()) // userId comes directly from the token
                .content(dto.getContent())
                .username(dto.getUsername())
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.parse(dto.getCreatedAt(), preciseFormat))
                .build();

        Message savedMessage = messageRepository.save(message);

        ActivityLogEvent event = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ServiceUsed")
                .eventTimestamp(Instant.now())
                .sourceSystem("ChatService")
                .version("1.0")
                .payload(ActivityLogEvent.Payload.builder()
                        .usage_log_id_source(message.getId())
                        .client_id(message.getSenderId())
                        .product_id(null)
                        .service_id(message.getChatId())
                        .usage_timestamp(message.getCreatedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                        )
                        .request_details(Map.of("content", message.getContent()))
                        .status("Completed")
                        .duration_ms(null)
                        .build()
                )
                .build();
        rabbitTemplate.convertAndSend("q_activity_logs", event);

        dto.setId(savedMessage.getId());
        return dto;
    }
}
