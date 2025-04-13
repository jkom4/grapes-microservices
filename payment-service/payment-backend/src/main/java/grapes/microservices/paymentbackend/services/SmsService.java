package grapes.microservices.paymentbackend.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    /**
     * Initialize Twilio client
     */
    private void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Send SMS with OTP to the user
     * @param phoneNumber the user's phone number
     * @param otp the one-time password
     * @return the message SID if sent successfully or a log message in development
     */
    public String sendOtp(String phoneNumber, String otp) {
        log.info("Sending OTP {} to phone number {}", otp, phoneNumber);

        // In development or testing, don't actually send SMS
        if ("development".equals(environment) || "test".equals(environment)) {
            log.info("Development/Test mode: Would send OTP {} to {}", otp, phoneNumber);
            return "DEV_MODE_" + otp;
        }

        try {
            initTwilio();

            String messageBody = "Your 3D Secure verification code is: " + otp;

            Message message = Message.creator(
                            new PhoneNumber(phoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            messageBody)
                    .create();

            log.info("SMS sent successfully, SID: {}", message.getSid());
            return message.getSid();
        } catch (Exception e) {
            log.error("Error sending SMS: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
}