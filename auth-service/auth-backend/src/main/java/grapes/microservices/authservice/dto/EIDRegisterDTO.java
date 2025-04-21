package grapes.microservices.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Required data for eID registration")
public class EIDRegisterDTO {

    @Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password (1 uppercase, 1 digit, 1 special character)", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?@])[A-Za-z\\d.;#!?@]+$",
            message = "Invalid password: must contain at least one uppercase letter, one digit and one special character")
    private String password;

    @Schema(description = "Phone number", example = "0460123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}