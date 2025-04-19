package grapes.microservices.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.models.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.Map;


@Data
public class UserDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotNull
    private boolean isActive;

    @NotNull(message = "name cannot be null")
    private String name;

    @NotNull(message = "firstname cannot be null")
    private String firstName;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?@])[A-Za-z\\d.;#!?@]+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

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

    @Size(min = 4, max = 4, message = "Pin code must be 4 digits long")
    @Pattern(regexp = "^\\d{4}$", message = "Pin code must contain only digits")
    private String pinCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPoints;

    private Role role;
    private String profession;
    private Map<AuthMethod, AuthMean> authMeans;

    @Valid
    private Address deliveryAddress;
    @Valid
    private Address billingAddress;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updatedAt;
}
