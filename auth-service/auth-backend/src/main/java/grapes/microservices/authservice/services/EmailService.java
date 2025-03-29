package grapes.microservices.authservice.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import grapes.microservices.authservice.dto.EmailDTO;
import grapes.microservices.authservice.utils.AuthLogger;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final String COMPANY_MAIL = "cameron.noupoue@student.hepl.be";

    private static final Logger logger = AuthLogger.getLogger();

    /**
     * Send an email to the recipient with the specified topic and message.
     * @param recipient the email address of the recipient
     * @param topic the topic of the email
     * @param message the message to send
     * @throws IOException if the email could not be sent
     */
    public void sendMail(EmailDTO recipient, String topic, String message) throws IOException {
        try {
            logger.info("Attempting to send email to: {}", recipient.getEmail());
            Email from = new Email(COMPANY_MAIL);
            Email to = new Email(recipient.getEmail());
            Content content = new Content("text/plain", message);
            Mail mail = new Mail(from, topic, to, content);

            SendGrid sg = new SendGrid(System.getenv("GRAPES_TWILIO_API_SECRET"));
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("Mail sent, status : " + response.getStatusCode());
            logger.info("Mail sent, body : " + response.getBody());
            logger.info("Mail sent, headers : " + response.getHeaders());
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", recipient.getEmail());
            throw e;
        }
    }
}
