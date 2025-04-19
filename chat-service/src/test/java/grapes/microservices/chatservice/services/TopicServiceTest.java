package grapes.microservices.chatservice.services;

import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.models.Chat;
import grapes.microservices.chatservice.repositories.ChatRepository;
import grapes.microservices.chatservice.repositories.MessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopicServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private TopicService topicService;

    public TopicServiceTest() {
        MockitoAnnotations.openMocks(this); // init mocks
    }

    @Test
    void testGetAllTopics_ReturnsListOfTopics() {
        // Given
        Chat chat = Chat.builder()
                .id("123")
                .topic("Test Topic")
                .build();
        when(chatRepository.findAll()).thenReturn(Collections.singletonList(chat));

        // When
        List<TopicDto> topics = topicService.getAllTopics();

        // Then
        assertNotNull(topics);
        assertEquals(1, topics.size());
        assertEquals("123", topics.getFirst().getId());
        assertEquals("Test Topic", topics.getFirst().getTopic());

        verify(chatRepository, times(1)).findAll();
    }
}
