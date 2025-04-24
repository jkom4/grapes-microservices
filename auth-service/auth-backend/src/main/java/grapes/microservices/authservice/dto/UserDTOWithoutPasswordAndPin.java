package grapes.microservices.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.models.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.Map;


@Data
public class UserDTOWithoutPasswordAndPin {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotNull
    private boolean isActive;

    @NotBlank(message = "name cannot be empty")
    private String name;

    @NotBlank(message = "firstname cannot be empty")
    private String firstName;

    private boolean isPasswordValid;

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    @Schema(description = "Email from the user", example = "user@example.com", format = "email")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean emailVerified;

    @NotNull(message = "phoneNumber cannot be null")
    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "The phone number must contain between 7 and 15 digits and may start with a '+'"
    )
    private String phoneNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean phoneVerified;

    @NotNull(message = "nationalId cannot be null")
    @Size(min = 11, max = 11, message = "National ID must be 11 digits long")
    @Pattern(regexp = "^\\d+$", message = "National ID must contain only digits")
    private String nationalId;

    @Past(message = "Birth date must be in the past")
    @NotNull(message = "birthDate cannot be null")
    private Date birthDate;

    private int age;

    @NotNull(message = "gender cannot be null")
    private Gender gender;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPoints;

    private Role role;
    private String profession;
    private Map<AuthMethod, AuthMean> authMeans;

    @Valid
    @NotNull(message = "deliveryAddress cannot be null")
    private Address deliveryAddress;

    @Valid
    @NotNull(message = "billingAddress cannot be null")
    private Address billingAddress;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updatedAt;
}
