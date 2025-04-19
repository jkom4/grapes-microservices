package grapes.microservices.chatservice.services;

import grapes.microservices.chatservice.config.RabbitMQConfig;
import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.models.Message;
import grapes.microservices.chatservice.repositories.ChatRepository;
import grapes.microservices.chatservice.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final ChatRepository    chatRepository;
    private final MessageRepository messageRepository;
    private final RabbitTemplate    rabbitTemplate;

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

    public MessageDto postMessage(String topicId, String userId, String content) {

        Message message = Message.builder()
                .chatId(topicId)
                .senderId(userId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        Message saved = messageRepository.save(message);


        MessageDto dto = MessageDto.builder()
                .userId(saved.getSenderId())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt().toString())
                .topicId(saved.getChatId())
                .build();

        String routingKey = "chat.to.room." + topicId;
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                routingKey,
                dto
        );

        return dto;
    }
}
