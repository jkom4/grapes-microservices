package grapes.microservices.authservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import grapes.microservices.authservice.services.EncryptionService;
import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Entity
@Data
@Document(collection = "users")
public class User {

    private static Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?@])[A-Za-z\\d.;#!?@]+$";

    private static final String PIN_PATTERN = "^\\d{4}$";

    @Field("_id")
    @Id
    private ObjectId id;

    @NotNull
    private boolean isActive;

    @NotNull
    private String name;

    @NotNull
    private String firstName;

    @NotNull(message = "password cannot be null")
    @Pattern(regexp = PASSWORD_PATTERN, message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    private boolean isPasswordValid;

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean emailVerified;

    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "The phone number must contain between 7 and 15 digits and may start with a '+'"
    )
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

    @Size(min = 4, max = 4, message = "Pin code must be 4 digits long")
    @Pattern(regexp = PIN_PATTERN, message = "Pin code must contain only digits")
    private String pinCode;

    @NotNull
    private Role role;

    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPoints;

    private String profession;

    private Map<AuthMethod, AuthMean> authMeans;

    @Embedded
    private Address deliveryAddress;

    @Embedded
    private Address billingAddress;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updatedAt;


    public String getFullName() {
        return this.firstName + " " + this.name;
    }

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
            int modifiers = field.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(modifiers) || java.lang.reflect.Modifier.isFinal(modifiers)) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(updatedUser);
                if (value != null) {
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
     *     <li>Encrypts the user's  PIN code using AES ({@link EncryptionService)}.</li>
     * </ul>
     * <p>
     * Note: BCrypt hashing is one-way and cannot be decrypted, whereas the encryption service is assumed to use a reversible encryption method.
     * </p>
     */
    public void encryptUser() throws Exception {
        try {
            encryptPassword(this.password);
            encryptPinCode(this.pinCode);
        } catch (Exception e) {
            logger.error("Error encrypting sensitive data for user with ID: {}: {}", this.id, e.getMessage());
            throw new Exception("Error encrypting user data", e);
        }
    }

    public void encryptPassword(String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void encryptPinCode(String pinCode) throws Exception {
        this.pinCode = EncryptionService.encrypt(pinCode);
    }

    public String decryptPinCode() throws Exception {
        return EncryptionService.decrypt(this.pinCode);
    }

    /**
     * Verify if a non-hashed password provided by the user matches the stored hashed password.
     * @param rawPassword the non-hashed password provided by the user
     */
    public boolean verifyUserPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    /**
     * Verifies if the hashed password matches the stored hashed password.
     * @param hashedPassword the hashed password to verify
     */
    public boolean verifyUserPasswordFromHash(String hashedPassword) {
        return this.password.equals(hashedPassword);
    }

    public boolean isPasswordFormatValid() {
        return this.password.matches(PASSWORD_PATTERN);
    }

    public static boolean isPasswordFormatValid(String password) {
        return password.matches(PASSWORD_PATTERN);
    }

    /**
     * Verifies if the provided raw PIN matches the stored encrypted PIN.
     * @param rawPin the raw PIN provided by the user
     */
    public boolean verifyUserPin(String rawPin) throws Exception {
        String storedPin = EncryptionService.decrypt(this.pinCode);
        return Objects.equals(storedPin, rawPin);
    }

    /**
     * Verifies if the provided hashed PIN matches the stored hashed PIN.
     * @param hashedPin the hashed PIN to verify
     * @return true if the hashed PIN matches the stored hashed PIN, false otherwise
     */
    public boolean verifyUserHashedPin(String hashedPin) {
        return this.pinCode.equals(hashedPin);
    }

    public boolean isPinFormatValid() {
        return isPinFormatValid(this.pinCode);
    }

    public static boolean isPinFormatValid(String pinCode) {
        return pinCode.matches(PIN_PATTERN);
    }

    public boolean hasRequiredAge(int requiredAge) {
        if (this.birthDate == null) return false;

        Calendar sixteenYearsAgo = Calendar.getInstance();
        sixteenYearsAgo.add(Calendar.YEAR, -requiredAge);

        return this.birthDate.before(sixteenYearsAgo.getTime());
    }

    /**
     * Checks if the national ID matches the birth date.
     * @return true if the national ID matches the birth date, false otherwise
     */
    public boolean nationalIdMatchesBirthDate() {
        if (this.nationalId == null || this.nationalId.length() < 6 || this.birthDate == null) {
            return false;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String birthDatePart = formatter.format(this.birthDate);

        String idDatePart = this.nationalId.substring(0, 6);

        return birthDatePart.equals(idDatePart);
    }
}

