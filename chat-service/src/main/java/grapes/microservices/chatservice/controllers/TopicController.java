package grapes.microservices.chatservice.controllers;

import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.dto.UserDto;
import grapes.microservices.chatservice.services.TopicService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class TopicController {

    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/topics")
    public List<TopicDto> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GetMapping("/topic/{id}/messages")
    public List<MessageDto> getMessagesByTopicId(@PathVariable String id) {
        return topicService.getMessagesByTopicId(id);
    }

    @PostMapping(value = "/topic/{id}/message", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public MessageDto postMessage(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String token = body.get("user-token");
        String content = body.get("content");

        return topicService.postMessage(id, token, content);
    }

    @GetMapping("/user")
    public UserDto getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String userId = null;
        String username = null;

        return new UserDto(userId, username);
    }
}
