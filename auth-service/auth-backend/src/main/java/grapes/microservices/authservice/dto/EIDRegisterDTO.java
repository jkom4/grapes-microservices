package grapes.microservices.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Required data for eID registration")
public class EIDRegisterDTO {

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?@])[A-Za-z\\d.;#!?@]+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Password cannot be null")
    private String password;

    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "The phone number must contain between 7 and 15 digits and may start with a '+'")
    @NotNull(message = "Phone number cannot be null")
    private String phoneNumber;
}