package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.util.Map;

import grapes.microservices.paymentbackend.utils.DataUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    @Value("${app.ports.acs}")
    private int acsPort;

    @Value("${app.keystore.client.path}")
    private String clientKeystorePath;

    @Value("${app.keystore.client.password}")
    private String clientKeystorePassword;

    @Value("${app.keystore.client.alias}")
    private String clientKeystoreAlias;

    @Value("${app.keystore.client.key.password}")
    private String clientKeyPassword;


    @Value("${app.truststore.acs.path}")
    private String acsTruststorePath;

    @Value("${app.truststore.acs.password}") // Inject the new property
    private String acsTruststorePassword;

    private static final String SOURCE = "client";

    /**
     * Send card details to ACS to initiate 3D Secure verification
     * @param paymentRequest the payment request with card details
     * @param user the authenticated user
     * @return the OTP code sent to the user's phone if successful, null otherwise
     */
    public String initiateCardVerification(PaymentRequestDTO paymentRequest, User user) {
        log.info("Initiating 3D Secure verification for user: {}", user.getLogin());

        try {
            // Format card data for ACS
            String cardData = "card=" + paymentRequest.getCardNumber() + "#date=" + paymentRequest.getExpirationDate();
            log.info("Card data prepared: {}", cardData);

            // Sign the data with client's private key
            PrivateKey privateKey = KeystoreUtils.getPrivateKey(
                    clientKeystorePath,
                    clientKeystorePassword,
                    clientKeystoreAlias,
                    clientKeyPassword
            );

            String signedData = SignUtils.signData(cardData, privateKey);
            log.info("Card data signed successfully");

            // Format data for ACS
            String formattedDataForAcs = "source=" + SOURCE + "&data=" + cardData + "&signature=" + signedData;

            // Send to ACS and get response
            String acsResponse = sendToACS(formattedDataForAcs);
            log.info("Received response from ACS: {}", acsResponse);

            if (acsResponse == null || acsResponse.isEmpty()) {
                log.error("Empty response from ACS");
                return null;
            }

            // Parse ACS response
            Map<String, String> parsedResponse = DataUtils.parseData(acsResponse);
            String otpCode = parsedResponse.get("data");

            if (otpCode == null || otpCode.isEmpty()) {
                log.error("No OTP code in ACS response");
                return null;
            }

            log.info("Card verification initiated successfully, OTP sent to user's phone");
            return otpCode;

        } catch (Exception e) {
            log.error("Error initiating card verification: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Send data to ACS
     * @param data the data to send
     * @return the response from ACS
     */
    private String sendToACS(String data) throws Exception {
        log.info("Sending data to ACS: {}", data);

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsPort,
                acsTruststorePath,
                acsTruststorePassword)) {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            // Send data to ACS
            writer.println(data);
            log.info("Data sent to ACS");

            // Read response
            String response = reader.readLine();
            log.info("Received response from ACS: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACS: {}", e.getMessage(), e);
            throw e;
        }
    }
}