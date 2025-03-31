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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * Service for sending emails
 */
@Service
@Getter
@RequiredArgsConstructor
public class EmailService {

    private final String COMPANY_MAIL = "cameron.noupoue@student.hepl.be";

    private static final Logger logger = AuthLogger.getLogger();

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null) {
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
    public void sendMail(String recipientMail, String topic, String message) throws IOException {
        try {
            logger.info("Attempting to send email to: {}", recipientMail);
            Email from = new Email(COMPANY_MAIL);
            Email to = new Email(recipientMail);
            topic = "Grapes Auth service - " + topic;
            message = "Hello,\n\n" + message + "\n\nBest regards,\nGRAPES Auth service";
            Content content = new Content("text/plain", message);
            Mail mail = new Mail(from, topic, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("Mail sent, status : " + response.getStatusCode());
            logger.info("Mail sent, body : " + response.getBody());
            logger.info("Mail sent, headers : " + response.getHeaders());
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", recipientMail);
            throw e;
        }
    }
}
