package grapes.microservices.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private String id;
    private String userId;
    private String username;
    private String content;
    private String createdAt;
    private String topicId;
}
