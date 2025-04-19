package grapes.microservices.authservice.utils.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credentials are incorrect.");
    }
}
