package grapes.microservices.authservice.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String bankId;
    private String clientId;
    private String name;
    private String familyName;
    private String passwordHash;
    private String email;
    private boolean emailVerified;
    private String phoneNumber;
    private boolean phoneVerified;
    private String nationalId;
    private LocalDate birthDate;
    private int age;
    private String gender;
    private String cardNumber;
    private String pinCode;
    private String role;
    private String profession;

    private Map<String, AuthMethod> authMethods;

    @DBRef
    private Address deliveryAddress;

    @DBRef
    private Address billingAddress;

    // Getters and Setters

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static class AuthMethod {
        private boolean enabled;
        private String publicKey; // only for eID
        private String challenge;
        private String token;     // only for masi_id
        private Integer counter;  // only for otp & calculator
        private LocalDate lastLogin;
        // Getters and Setters
    }
}
