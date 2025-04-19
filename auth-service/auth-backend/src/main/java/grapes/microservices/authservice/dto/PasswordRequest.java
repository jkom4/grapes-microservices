package grapes.microservices.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordRequest {

    private String currentPassword;
    private String updatedPassword;
}
