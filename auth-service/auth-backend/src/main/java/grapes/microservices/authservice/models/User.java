package grapes.microservices.authservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.services.EncryptionService;
import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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

@Entity
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

    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPoints;

    private String profession;

    private Map<String, AuthMean> authMethods;

    @Embedded
    private Address deliveryAddress;

    @Embedded
    private Address billingAddress;


    /**
     * Updates the current user object with non-null properties from the provided updatedUser object.
     * Only the fields of updatedUser that are not null will overwrite the corresponding fields in the current user.
     *
     * @param updatedUser The user object containing updated values.
     *                    Only non-null properties will be copied to the current user object.
     * @throws RuntimeException if an error occurs while accessing or updating fields.
     */
    public void update(User updatedUser) {
        if (updatedUser == null) {
            logger.warn("Attempted to update user with a null object.");
            return;
        }

        logger.info("Starting update process for user with ID: {}", this.id);
        for (java.lang.reflect.Field field : User.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(updatedUser);
                if (value != null) {
                    logger.debug("Updating field: {} with value: {}", field.getName(), value);
                    field.set(this, value);
                }
            } catch (IllegalAccessException e) {
                logger.error("Error while accessing field {}: {}", field.getName(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
        logger.info("User update process completed for user with ID: {}", this.id);
    }


    /**
     * Encrypts the user's sensitive data.
     * <p>
     * This method performs two types of encryption:
     * <ul>
     *     <li>Encrypts the user's password using BCrypt hashing.</li>
     *     <li>Encrypts sensitive fields such as national ID, card number, and PIN code using {@link EncryptionService}.</li>
     * </ul>
     * <p>
     * Note: BCrypt hashing is one-way and cannot be decrypted, whereas the encryption service is assumed to use a reversible encryption method.
     * </p>
     */
    public void encryptUser() throws Exception {
        logger.info("Starting encryption process for user with ID: {}", this.id);

        //encrypt password
        this.password = passwordEncoder.encode(password);
        logger.debug("Password for user with ID: {} successfully encrypted.", this.id);

        //encrypt sensitive data (nationalId, cardNumber, pinCode)
        try {
            EncryptionService.encrypt(this.nationalId);
            if (cardNumber != null) {
                EncryptionService.encrypt(this.cardNumber);
            }
            if (pinCode != null) {
                EncryptionService.encrypt(this.pinCode);
            }
        } catch (Exception e) {
            logger.error("Error encrypting sensitive data for user with ID: {}: {}", this.id, e.getMessage());
            throw new Exception("Error encrypting user data", e);
        }
    }

    /**
     * Verify if a password provided by the user matches the stored hashed password.
     * @param rawPassword the password provided by the user
     * @return true if the password matches the stored hashed password, false otherwise
     */
    public boolean verifyPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}

