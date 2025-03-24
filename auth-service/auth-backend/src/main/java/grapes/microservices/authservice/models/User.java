package grapes.microservices.authservice.models;

import com.mongodb.lang.Nullable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

@Entity
@Data
@Document(collection = "users")
public class User {

    @Field("_id")
    @Id
    private ObjectId id;

    @NotNull
    private String bankId;

    @NotNull
    private String clientId;

    @NotNull
    private String name;

    @NotNull
    private String firstName;

    @NotNull(message = "password cannot be null")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.;#!?])[A-Za-z\\d.;#!?]+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotNull(message = "email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    private boolean emailVerified;

    private String phoneNumber;

    private boolean phoneVerified;

    @Size(min = 11, max = 11, message = "National ID must be 11 digits long")
    @NotNull(message = "nationalId cannot be null")
    private String nationalId;

    @NotNull
    @Past(message = "Birth date must be in the past")
    private Date birthDate;

    private double age;

    @NotNull
    private Gender gender;

    @Size(min = 16, max = 16, message = "Card number must be 16 digits long")
    private String cardNumber;

    @Size(min = 4, max = 4, message = "Pin code must be 4 digits long")
    private String pinCode;

    @NotNull
    private Role role;

    private String profession;

    private Map<String, AuthMethod> authMethods;

    @Embedded
    @NotNull
    private Address deliveryAddress;

    @Embedded
    @NotNull
    private Address billingAddress;

    public void update(User updatedUser) {
        this.bankId = updatedUser.bankId;
        this.name = updatedUser.name;
        this.firstName = updatedUser.firstName;
        this.email = updatedUser.email;
        this.emailVerified = updatedUser.emailVerified;
        this.phoneNumber = updatedUser.phoneNumber;
        this.phoneVerified = updatedUser.phoneVerified;
        this.nationalId = updatedUser.nationalId;
        this.birthDate = updatedUser.birthDate;
        this.gender = updatedUser.gender;
        this.cardNumber = updatedUser.cardNumber;
        this.pinCode = updatedUser.pinCode;
        this.role = updatedUser.role;
        this.profession = updatedUser.profession;
        this.authMethods = updatedUser.authMethods;
        this.deliveryAddress = updatedUser.deliveryAddress;
        this.billingAddress = updatedUser.billingAddress;
    }
}

