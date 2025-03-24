package grapes.microservices.authservice.models;

import lombok.Data;

import java.util.Date;

@Data
public class AuthMethod {
    private boolean enabled;
    private String publicKey;
    private String challenge;
    private Date lastLogin;
    private Integer counter; // For OTP, calc OTP, etc.
    private String token; // For MASI-ID
}
