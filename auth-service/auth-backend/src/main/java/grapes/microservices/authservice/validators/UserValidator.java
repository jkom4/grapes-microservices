package grapes.microservices.authservice.validators;

import grapes.microservices.authservice.models.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * UserValidator provides a validator for User entities.
 * This validator ensures that a user respects all constraints before processing.
 * @author Cameron
 */
@Component
@Data
public class UserValidator {

    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Check if a user respects all constraints
     *
     * @param user the user to validate
     * @throws IllegalArgumentException if a field is invalid
     */
    public static boolean isValid(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<User> violation : violations) {
                errorMessage.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }
        return true;
    }
}
