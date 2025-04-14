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
import java.util.UUID;

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

    @Value("${app.truststore.acs.password}")
    private String acsTruststorePassword;

    private static final String SOURCE = "client";

    /**
     * Send card details to ACS to initiate 3D Secure verification
     * @param paymentRequest the payment request with card details
     * @param user the authenticated user
     * @param transactionId unique identifier for this transaction
     * @return the OTP code sent to the user's phone if successful, null otherwise
     */
    public String initiateCardVerification(PaymentRequestDTO paymentRequest, User user, String transactionId) {
        log.info("Initiating 3D Secure verification for user: {}, transaction: {}", user.getLogin(), transactionId);

        try {
            // Format card data for ACS including transaction ID and merchant name
            String cardData = String.format(
                    "card=%s#date=%s#amount=%s#merchant=%s#transactionId=%s",
                    paymentRequest.getCardNumber(),
                    paymentRequest.getExpirationDate(),
                    paymentRequest.getAmount(),
                    paymentRequest.getMerchantName(),
                    transactionId
            );
            log.info("Card data prepared for transaction {}", transactionId);

            // Sign the data with client's private key
            PrivateKey privateKey = KeystoreUtils.getPrivateKey(
                    clientKeystorePath,
                    clientKeystorePassword,
                    clientKeystoreAlias,
                    clientKeyPassword
            );

            String signedData = SignUtils.signData(cardData, privateKey);
            log.info("Card data signed successfully for transaction {}", transactionId);

            // Format data for ACS
            String formattedDataForAcs = "source=" + SOURCE + "&data=" + cardData + "&signature=" + signedData;

            // Send to ACS and get response
            String acsResponse = sendToACS(formattedDataForAcs);
            log.info("Received response from ACS for transaction {}: {}", transactionId, acsResponse);

            if (acsResponse == null || acsResponse.isEmpty()) {
                log.error("Empty response from ACS for transaction {}", transactionId);
                return null;
            }

            // Parse ACS response
            Map<String, String> parsedResponse = DataUtils.parseData(acsResponse);
            String responseSource = parsedResponse.get("source");
            String otpCode = parsedResponse.get("data");
            String signature = parsedResponse.get("signature");
            String responseTransactionId = parsedResponse.get("transactionId");

            // Validate response
            if (!"acs".equals(responseSource) || otpCode == null || signature == null) {
                log.error("Invalid response format from ACS for transaction {}", transactionId);
                return null;
            }

            // Validate transaction ID if provided in response
            if (responseTransactionId != null && !transactionId.equals(responseTransactionId)) {
                log.error("Transaction ID mismatch: expected {}, got {}", transactionId, responseTransactionId);
                return null;
            }

            // Return the OTP code
            log.info("Card verification initiated successfully for transaction {}, OTP sent to user's phone", transactionId);
            return otpCode;

        } catch (Exception e) {
            log.error("Error initiating card verification for transaction {}: {}", transactionId, e.getMessage(), e);
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