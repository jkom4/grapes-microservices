package grapes.microservices.authservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.services.EncryptionService;
import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.Map;

@Data
@Document(collection = "users")
public class User {

    private static Logger logger = LoggerFactory.getLogger(AuthLogger.class);
    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Field("_id")
    @Id
    private ObjectId id;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isActive;

    @NotNull
    private String bankId;

    @NotNull
    private String name;

    @NotNull
    private String firstName;

    @NotNull(message = "password cannot be null")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?@])[A-Za-z\\d.;#!?@]+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean emailVerified;

    private String phoneNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean phoneVerified;

    @Size(min = 11, max = 11, message = "National ID must be 11 digits long")
    @NotNull(message = "nationalId cannot be null")
    @Pattern(regexp = "^\\d{11}$", message = "National ID must contain only digits")
    private String nationalId;

    @NotNull
    @Past(message = "Birth date must be in the past")
    private Date birthDate;

    @NotNull(message = "gender cannot be null")
    private Gender gender;

    @Size(min = 16, max = 16, message = "Card number must be 16 digits long")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must contain only digits")
    private String cardNumber;

    @Size(min = 4, max = 4, message = "Pin code must be 4 digits long")
    @Pattern(regexp = "^\\d{4}$", message = "Pin code must contain only digits")
    private String pinCode;

    @NotNull
    private Role role;

    private String profession;
    private Map<String, AuthMean> authMethods;

    private Address deliveryAddress;
    private Address billingAddress;

    public void update(User updatedUser) {
        if (updatedUser == null) return;

        for (java.lang.reflect.Field field : User.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(updatedUser);
                if (value != null) {
                    field.set(this, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void encryptUser() throws Exception {
        this.password = passwordEncoder.encode(password);
        EncryptionService.encrypt(this.nationalId);
        if (cardNumber != null) EncryptionService.encrypt(this.cardNumber);
        if (pinCode != null) EncryptionService.encrypt(this.pinCode);
    }

    public boolean verifyPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
