package grapes.microservices.authservice.models;

import lombok.Data;

import java.util.Date;

/**
 * Authentication means and actual situaion (enabled, last login, etc.) for a user.
 */
@Data
public class AuthMean {
    private boolean enabled;
    private AuthMethod name;
    private String publicKey;
    private String challenge;
    private Date lastLogin;
    private Integer counter; // For OTP, calc OTP, etc.
    private String token; // For MASI-ID
}
