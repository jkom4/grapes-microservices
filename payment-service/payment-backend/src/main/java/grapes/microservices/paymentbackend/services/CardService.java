package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import grapes.microservices.paymentbackend.utils.DataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * Service for handling card-related operations, particularly 3D Secure verification
 * during payment processing. Facilitates communication with the Authentication Server (ACS).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    // Main ACS server port (for initiation)
    @Value("${app.ports.acs}")
    private int acsPort;

    // Client keystore configuration for signing requests
    @Value("${app.keystore.client.path}")
    private String clientKeystorePath;
    @Value("${app.keystore.client.password}")
    private String clientKeystorePassword;
    @Value("${app.keystore.client.alias}")
    private String clientKeystoreAlias;
    @Value("${app.keystore.client.key.password}")
    private String clientKeyPassword;

    // Client truststore configuration for trusting ACS server
    @Value("${app.truststore.client.path}")
    private String clientTruststorePathForAcs;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePasswordForAcs;
    @Value("${app.truststore.acs.alias:acs_trusted}")
    private String acsTrustedAlias;

    // Source identifier for messages sent to ACS
    private static final String SOURCE = "client";

    /**
     * Sends card details to ACS to initiate 3D Secure verification.
     * Signs the request with the client's private key and processes the ACS response.
     *
     * @param paymentRequest The payment request with card details
     * @param client The authenticated client
     * @param paymentAttemptId Unique identifier for this payment attempt
     * @return The OTP code received from ACS if successful, null otherwise
     */
    public String initiateCardVerification(PaymentRequestDTO paymentRequest, Client client, String paymentAttemptId) {
        log.info("Initiating 3D Secure verification for client: {}, payment attempt ID: {}", client.getEmail(), paymentAttemptId);

        try {
            // Prepare card data
            String cardData = prepareCardData(paymentRequest, paymentAttemptId);

            // Sign data
            String signedData = signCardData(cardData);

            // Format and send to ACS
            String formattedDataForAcs = formatDataForAcs(cardData, signedData);
            String acsResponse = sendToACS(formattedDataForAcs);

            // Process ACS response
            return processAcsResponse(acsResponse, paymentAttemptId);
        } catch (Exception e) {
            log.error("Error initiating card verification for payment attempt {}: {}", paymentAttemptId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Prepares card data string for ACS verification
     */
    private String prepareCardData(PaymentRequestDTO paymentRequest, String paymentAttemptId) {
        return String.format(
                "card=%s#date=%s#amount=%s#merchant=%s#transactionId=%s",
                paymentRequest.getCardNumber(),
                paymentRequest.getExpirationDate(),
                paymentRequest.getAmount(),
                paymentRequest.getMerchantName(),
                paymentAttemptId
        );
    }

    /**
     * Signs card data using client's private key
     */
    private String signCardData(String cardData) throws Exception {
        PrivateKey privateKey = KeystoreUtils.getPrivateKey(
                clientKeystorePath,
                clientKeystorePassword,
                clientKeystoreAlias,
                clientKeyPassword
        );
        return SignUtils.signData(cardData, privateKey);
    }

    /**
     * Formats data for ACS with source and signature
     */
    private String formatDataForAcs(String cardData, String signedData) {
        return "source=" + SOURCE + "&data=" + cardData + "&signature=" + signedData;
    }

    /**
     * Processes the ACS response and extracts/verifies OTP
     */
    private String processAcsResponse(String acsResponse, String paymentAttemptId) throws Exception {
        if (acsResponse == null || acsResponse.isEmpty() || acsResponse.startsWith("ERROR:")) {
            log.error("Empty or error response from ACS for payment attempt {}: {}", paymentAttemptId, acsResponse);
            return null;
        }

        Map<String, String> parsedResponse = DataUtils.parseData(acsResponse);
        String responseSource = parsedResponse.get("source");
        String otpCode = parsedResponse.get("data"); // OTP generated by ACS
        String signature = parsedResponse.get("signature"); // OTP signature by ACS

        if (!"acs".equals(responseSource) || otpCode == null || signature == null) {
            log.error("Invalid response format from ACS for payment attempt {}: {}", paymentAttemptId, acsResponse);
            return null;
        }

        // Verify ACS signature
        if (verifyAcsSignature(otpCode, signature, paymentAttemptId)) {
            log.info("Card verification initiated successfully for payment attempt {}, OTP should be sent to client's phone by ACS.", paymentAttemptId);
            return otpCode;
        }

        return null;
    }

    /**
     * Verifies ACS signature on the OTP
     */
    private boolean verifyAcsSignature(String otpCode, String signature, String paymentAttemptId) throws Exception {
        log.debug("CardService: Attempting to verify ACS signature for attempt {} using alias '{}' from truststore '{}'",
                paymentAttemptId, acsTrustedAlias, clientTruststorePathForAcs);
        Certificate acsCertificate = KeystoreUtils.getCertificate(
                clientTruststorePathForAcs,
                clientTruststorePasswordForAcs,
                acsTrustedAlias
        );
        PublicKey acsPublicKey = acsCertificate.getPublicKey();

        if (!SignUtils.verifySignature(otpCode, signature, acsPublicKey)) {
            log.error("CardService: Invalid ACS signature received for payment attempt {}! Data='{}', Signature='{}'",
                    paymentAttemptId, otpCode, signature);
            return false;
        }
        log.info("CardService: ACS response signature verified successfully for payment attempt {}.", paymentAttemptId);
        return true;
    }

    /**
     * Sends data to ACS using SSL client socket.
     * Establishes secure connection with ACS server for communication.
     *
     * @param data The formatted data string to send
     * @return The response string from ACS, or null/error on failure
     * @throws Exception If communication with ACS fails
     */
    private String sendToACS(String data) throws Exception {
        log.info("Sending data to ACS on port {} using truststore {}", acsPort, clientTruststorePathForAcs);

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsPort,
                clientTruststorePathForAcs,
                clientTruststorePasswordForAcs)) {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            writer.println(data);
            log.info("Data sent to ACS");
            String response = reader.readLine();
            log.info("Response received from ACS");
            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACS on port {}: {}", acsPort, e.getMessage(), e);
            throw e;
        }
    }
}