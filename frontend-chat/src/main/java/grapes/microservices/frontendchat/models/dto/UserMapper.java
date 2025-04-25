package grapes.microservices.frontendchat.models.dto;
import grapes.microservices.frontendchat.models.User;

public class UserMapper {
    public static User toEntity(UserDTO dto) {
        return new User(
                dto.getId(),
                dto.getUsername()
        );
    }
}
