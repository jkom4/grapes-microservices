package grapes.microservices.authservice.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for sending emails
 * This service uses the SendGrid API to send emails
 * The SendGrid API key must be configured in the application.properties file
 * @see <a href="https://sendgrid.com/docs/API_Reference/api_v3.html">SendGrid API</a>
 * @author Cameron
 */
@Service
@Getter
@RequiredArgsConstructor
public class EmailService {

    @Value("${company.mail}")
    private String COMPANY_MAIL;

    private final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("SendGrid API key is not configured properly.");
            System.err.println("SendGrid API key is not configured properly.");
        }
    }

    /**
     * Send an email to the recipient with the specified topic and message.
     * @param recipientMail the email address of the recipient
     * @param topic the topic of the email
     * @param message the message to send
     * @throws IOException if the email could not be sent
     */
    public boolean sendMail(String recipientMail, String topic, String message) throws IOException {
        try {
            if ( !isValidEmail(recipientMail) || !isValidEmail( COMPANY_MAIL ) ) {
                throw new IllegalArgumentException("Invalid email address : " + recipientMail + " or " + COMPANY_MAIL);
            }
            logger.info("Attempting to send email to: {}", recipientMail);
            Email from = new Email(COMPANY_MAIL);
            Email to = new Email(recipientMail);
            topic = "Grapes Auth service - " + topic;
            message = "Hello,\n\n" + message + "\n\nBest regards,\nGrapes Auth service";
            Content content = new Content("text/plain", message);
            Mail mail = new Mail(from, topic, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("Mail sent, status : " + response.getStatusCode());
            logger.info("Mail sent, headers : " + response.getHeaders());
            return response.getStatusCode() == 202;
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", recipientMail);
            throw e;
        }
    }

    /**
     * Check if the email address is valid
     * @param email the email address to check
     * @return true if the email address is valid
     */
    private static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
