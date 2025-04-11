package grapes.microservices.chatservice.controllers;

import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/topics")
    public List<TopicDto> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GetMapping("/topic/{id}/messages")
    public List<MessageDto> getMessagesByTopicId(@PathVariable String id) {
        return topicService.getMessagesByTopicId(id);
    }

    @PostMapping("/topic/{id}/message")
    public MessageDto postMessage(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String token = body.get("user-token");
        String content = body.get("content");

        return topicService.postMessage(id, token, content);
    }
}
