package grapes.microservices.authservice.utils.exceptions;

import grapes.microservices.authservice.dto.JsonMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.FieldError;

/**
 * Global exception handler for validation errors triggered by @Valid annotations in controller methods.
 *
 * @author Cameron
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<JsonMessage> handleUserNotActiveException(UserNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonMessage(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<JsonMessage> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonMessage(ex.getMessage()));
    }

    @ExceptionHandler(ChallengeSendFailedException.class)
    public ResponseEntity<JsonMessage> handleChallengeSendFailed(ChallengeSendFailedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonMessage(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<JsonMessage> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonMessage(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonMessage> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonMessage("An internal error occurred: " + ex.getMessage()));
    }
}
