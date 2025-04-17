package grapes.microservices.frontendchat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageDTO {
    private int topicId;
    private int userId;
    private String username;
    private String content;
    private String createdAt;
}
