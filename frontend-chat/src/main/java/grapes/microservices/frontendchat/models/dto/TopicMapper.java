package grapes.microservices.frontendchat.models.dto;

import grapes.microservices.frontendchat.models.Topic;

import java.util.List;
import java.util.stream.Collectors;

public class TopicMapper {
    public static Topic toEntity(TopicDTO dto) {
        return new Topic(
                Integer.parseInt(dto.getId()),
                dto.getTopic(),
                ""
        );
    }

    public static List<Topic> toEntityList(List<TopicDTO> dtoList) {
        return dtoList.stream()
                .map(TopicMapper::toEntity)
                .collect(Collectors.toList());
    }
}
