package grapes.microservices.paymentbackend.services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * Service for sending SMS messages using Twilio.
 * Provides OTP delivery capabilities with environment-aware behavior.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${app.environment:production}")
    private String environment;

    private boolean twilioInitialized = false;

    /**
     * Initializes the Twilio client once after bean creation if in production environment.
     * Skips initialization in development/test environments.
     */
    @PostConstruct
    public void initTwilio() {
        if (!isDevelopmentOrTest()) {
            try {
                // Check if credentials are present
                if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
                    log.warn("Twilio credentials (SID or Token) are missing. SMS sending will be disabled.");
                    return;
                }
                Twilio.init(accountSid, authToken);
                twilioInitialized = true;
                log.info("Twilio client initialized successfully for production environment.");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio client: {}", e.getMessage());
            }
        } else {
            log.info("Running in {} environment. Skipping Twilio initialization.", environment);
        }
    }

    /**
     * Sends an SMS with OTP to the specified phone number.
     * In development/test environments, logs the action instead of actually sending.
     *
     * @param rawPhoneNumber The user's phone number (should include country code, e.g., +32...)
     * @param otp The one-time password to send
     * @throws RuntimeException if SMS sending fails in production environment
     */
    public void sendOtp(String rawPhoneNumber, String otp) {
        log.info("Attempting to send OTP {} to phone number {}", otp, rawPhoneNumber);

        // Skip actual sending in dev/test environments
        if (isDevelopmentOrTest()) {
            log.warn("DEVELOPMENT/TEST MODE: Would send OTP '{}' to phone number '{}'", otp, rawPhoneNumber);
            return;
        }

        // Verify Twilio initialization and valid phone numbers
        if (!twilioInitialized) {
            log.error("Twilio client not initialized. Cannot send SMS.");
            throw new RuntimeException("SMS Service (Twilio) is not configured or failed to initialize.");
        }
        if (twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            log.error("Twilio 'from' phone number is not configured.");
            throw new RuntimeException("Twilio 'from' phone number is missing.");
        }
        if (rawPhoneNumber == null || rawPhoneNumber.isEmpty()) {
            log.error("Target phone number is null or empty.");
            throw new RuntimeException("Target phone number for OTP is missing.");
        }

        // Format phone number to ensure E.164 format (e.g., +32XXXXXXXXX)
        String targetPhoneNumber = rawPhoneNumber.startsWith("+") ? rawPhoneNumber : "+" + rawPhoneNumber;

        try {
            // Create and send the message
            String messageBody = "Your Grapes Bank 3D Secure verification code is: " + otp;

            Message message = Message.creator(
                            new PhoneNumber(targetPhoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            messageBody)
                    .create();

            log.info("SMS sent successfully via Twilio, SID: {}", message.getSid());

        } catch (ApiException e) {
            // Handle Twilio-specific errors (invalid number, unroutable, etc.)
            log.error("Twilio API error sending SMS to {}: Code={}, Message='{}', MoreInfo='{}'",
                    targetPhoneNumber, e.getCode(), e.getMessage(), e.getMoreInfo());
            throw new RuntimeException("Failed to send SMS via Twilio: " + e.getMessage(), e);
        } catch (Exception e) {
            // Handle other potential errors
            log.error("Generic error sending SMS to {}: {}", targetPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Determines if the current environment is development or test.
     *
     * @return true if in development or test environment, false otherwise
     */
    private boolean isDevelopmentOrTest() {
        return "development".equalsIgnoreCase(environment) || "test".equalsIgnoreCase(environment);
    }
}