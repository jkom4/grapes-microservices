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

    private String id;         // id message
    private String userId;     // id user
    private String username;   // username
    private String content;    // message
    private String createdAt;  // date
    private String topicId;    // id topic
}
