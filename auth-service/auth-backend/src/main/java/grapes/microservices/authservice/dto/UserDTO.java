package grapes.microservices.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.models.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Date;
import java.util.Map;


@Data
public class UserDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isActive;

    @NotNull(message = "bankId cannot be null")
    private String bankId;

    @NotNull(message = "name cannot be null")
    private String name;

    @NotNull(message = "firstname cannot be null")
    private String firstName;

    @NotNull(message = "password cannot be null")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?])[A-Za-z\\d.;#!?]+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    @Schema(description = "Email from the user", example = "user@example.com", format = "email")
    private String email;

    @Null
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean emailVerified;

    @NotNull(message = "phoneNumber cannot be null")
    @Column(unique = true)
    private String phoneNumber;

    @Null
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean phoneVerified;

    @NotNull(message = "nationalId cannot be null")
    @Size(min = 11, max = 11, message = "National ID must be 11 digits long")
    @Pattern(regexp = "^\\d{11}$\n", message = "National ID must contain only digits")
    private String nationalId;

    @Past(message = "Birth date must be in the past")
    @NotNull(message = "birthDate cannot be null")
    private Date birthDate;

    private double age;

    @NotNull(message = "gender cannot be null")
    private Gender gender;

    @Size(min = 16, max = 16, message = "Card number must be 16 digits long")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must contain only digits")
    private String cardNumber;

    @Size(min = 4, max = 4, message = "Pin code must be 4 digits long")
    @Pattern(regexp = "^\\d{4}$", message = "Pin code must contain only digits")
    private String pinCode;

    private Role role;
    private String profession;
    private Map<String, AuthMean> authMethods;
    private Address deliveryAddress;
    private Address billingAddress;
}
