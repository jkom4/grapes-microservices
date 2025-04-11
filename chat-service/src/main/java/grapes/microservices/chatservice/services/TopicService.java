package grapes.microservices.chatservice.services;

import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.models.Chat;
import grapes.microservices.chatservice.models.Message;
import grapes.microservices.chatservice.repositories.ChatRepository;
import grapes.microservices.chatservice.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final AuthService authService;

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

    public MessageDto postMessage(String topicId, String token, String content) {
        var userDto = authService.validateToken(token);

        if (userDto == null) {
            throw new RuntimeException("Invalid Tokens");
        }

        Message message = Message.builder()
                .chatId(topicId)
                .senderId(userDto.getId())
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        return MessageDto.builder()
                .userId(savedMessage.getSenderId())
                .content(savedMessage.getContent())
                .createdAt(savedMessage.getCreatedAt().toString())
                .topicId(savedMessage.getChatId())
                .build();
    }
}
