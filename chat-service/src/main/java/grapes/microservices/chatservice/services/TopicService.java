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
                        .userId(message.getSenderId())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt().toString())
                        .topicId(message.getChatId())
                        .build())
                .collect(Collectors.toList());
    }

    public void postMessage(String topicId, String userId, String content) {
        Message saved = messageRepository.save(
                Message.builder()
                        .chatId(topicId)
                        .senderId(userId)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        ActivityLogEvent event = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ServiceUsed")
                .eventTimestamp(Instant.now())
                .sourceSystem("ChatService")
                .version("1.0")
                .payload(ActivityLogEvent.Payload.builder()
                        .usage_log_id_source(saved.getId())
                        .client_id(userId)
                        .product_id(null)
                        .service_id(topicId)
                        .usage_timestamp(saved.getCreatedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                        )
                        .request_details(Map.of("content", content))
                        .status("Completed")
                        .duration_ms(null)
                        .build()
                )
                .build();
        rabbitTemplate.convertAndSend("q_activity_logs", event);
    }
}
