package grapes.microservices.authservice.services;

import grapes.microservices.authservice.utils.AuthLogger;
import grapes.microservices.authservice.utils.challenge_request_limiter.OneCallPerMinutePerUser;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Service for sending SMS messages
 * This service uses the Twilio API to send SMS messages
 * @author Cameron
 */
@Service
@Getter
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @Value("${twilio.api.key}")
    private String API_KEY;

    @Value("${twilio.account.sid}")
    public String ACCOUNT_SID;

    @Value("${twilio.from.number}")
    private String TWILIO_PHONE_NUMBER;

    @PostConstruct
    public void init() {
        if (API_KEY == null || API_KEY.isBlank()) {
            logger.error("Twilio API key is not configured properly.");
            System.err.println("Twilio API key is not configured properly.");
        }
        if (ACCOUNT_SID == null || ACCOUNT_SID.isBlank()) {
            logger.error("Twilio Account SID is not configured properly.");
            System.err.println("Twilio Account SID is not configured properly.");
        }
        if (TWILIO_PHONE_NUMBER == null || TWILIO_PHONE_NUMBER.isBlank()) {
            logger.error("Twilio Phone Number is not configured properly.");
            System.err.println("Twilio Phone Number is not configured properly.");
        }
        Twilio.init(ACCOUNT_SID, API_KEY);
        logger.info("Twilio API initialized successfully.");
    }

    /**
     * Send an SMS message to the recipient with the specified message.
     * @param recipientPhoneNumber the phone number of the recipient
     * @param content the message to send
     */
    @OneCallPerMinutePerUser
    public boolean sendSms(String recipientPhoneNumber, String content) {
        // Check if the phone number is valid
        if (!isValidPhoneNumber(recipientPhoneNumber, "+32")) {
            logger.error("Invalid phone number: " + recipientPhoneNumber);
            throw new IllegalArgumentException("Invalid phone number: " + recipientPhoneNumber);
        }

        try {
            content = "Hello,\n\n " + content;
            Message message = Message
                    .creator(new PhoneNumber(recipientPhoneNumber), // to
                            new PhoneNumber(TWILIO_PHONE_NUMBER), // from
                            content)
                    .create();

            logger.info("SMS sent successfully: " + message.getSid());System.out.println(message);
            return message.getBody() != null;
        } catch (Exception e) {
            logger.error("Failed to send SMS: " + e.getMessage());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    /**
     * Check if the phone number is valid
     * @param phoneNumber the phone number to check
     * @param countryCode the country code to check (+32, +33, etc.)
     * @return true if the phone number is valid, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        // E.164 format + 8 to 15 digits
        boolean isE164 = phoneNumber.matches("^\\+\\d{8,15}$");
        boolean countryRespected = phoneNumber.startsWith(countryCode);

        return isE164 && countryRespected;
    }
}
