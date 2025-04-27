package grapes.microservices.frontendchat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageDTO {
    private String id;
    private String topicId;
    private String userId;
    private String username;
    private String content;
    private String createdAt;
}
