package grapes.microservices.authservice.utils.exceptions;

public class ChallengeSendFailedException extends RuntimeException {
    public ChallengeSendFailedException() {
        super("Failed to send challenge to the user.");
    }

    public ChallengeSendFailedException(String message) {
        super(message);
    }
}
