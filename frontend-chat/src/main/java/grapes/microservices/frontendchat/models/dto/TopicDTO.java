package grapes.microservices.frontendchat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TopicDTO{
    private String id;
    private String topic;
}