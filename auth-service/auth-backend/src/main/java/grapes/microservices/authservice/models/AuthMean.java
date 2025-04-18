package grapes.microservices.authservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Authentication means and actual situation (enabled, last login, etc.) for a user.
 */
@Data
@AllArgsConstructor
public class AuthMean {
    private boolean enabled;
    private Date lastLogin;
    private Integer counter;
}
