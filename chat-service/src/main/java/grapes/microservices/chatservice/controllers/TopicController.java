package grapes.microservices.chatservice.controllers;

import com.pusher.rest.Pusher;
import com.pusher.rest.data.Result;
import grapes.microservices.chatservice.dto.MessageDto;
import grapes.microservices.chatservice.dto.TopicDto;
import grapes.microservices.chatservice.dto.UserDto;
import grapes.microservices.chatservice.services.TokenService;
import grapes.microservices.chatservice.services.TopicService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class TopicController {

    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    @Autowired
    private Pusher pusher;
    @Autowired
    private TokenService tokenService;

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        if (!tokenService.isValidToken(token)) {
            return ResponseEntity.status(400).body("Invalid token");
        }
        var user = new UserDto(tokenService.extractUserId(token), tokenService.extractUserName(token));
        return ResponseEntity.ok(user);
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
    public ResponseEntity<?> postMessage(
            @PathVariable String id,
            @RequestBody MessageDto message
    ) {
        // 1. save message to database
        var savedMessage = topicService.postMessage(message);

        // 2. send message to pusher, so all listener clients will receive the message
        Result result = pusher.trigger(id, "new message", savedMessage);

        if (result.getStatus() == Result.Status.SUCCESS) {
            System.out.println("Message send with succès to Pusher.");
            return ResponseEntity.ok(savedMessage);
        } else {
            System.err.println("Error while sending message to pusher: " + result.getMessage() + " | Status: " + result.getHttpStatus());
            return ResponseEntity.status(500).body("Erreur lors de l'envoi à Pusher: " + result.getMessage());
        }
    }
}
