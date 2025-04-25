package grapes.microservices.chatservice.controllers;

import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.services.TopicService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/topic/{id}/message")
    @Transactional
    public ResponseEntity<Void> postMessage(
            @PathVariable("id") String topicId,
            @RequestBody Map<String, String> body
    ) {
        String userToken = body.get("user-token");
        String content   = body.get("content");
        topicService.postMessage(topicId, userToken, content);

        return ResponseEntity.noContent().build();
    }
}
