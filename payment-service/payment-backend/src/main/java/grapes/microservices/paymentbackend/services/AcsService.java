package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for communicating with the Authentication Control Server (ACS)
 * during payment processing for 3D Secure verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcsService {

    private static final String SOURCE = "client";

    @Value("${app.ports.acs}")
    private int acsPort;

    // Client keystore and truststore configuration
    @Value("${app.keystore.client.path}")
    private String clientKeystorePath;
    @Value("${app.keystore.client.password}")
    private String clientKeystorePassword;
    @Value("${app.keystore.client.alias}")
    private String clientKeystoreAlias;
    @Value("${app.keystore.client.key.password}")
    private String clientKeyPassword;

    // Truststore used by this client to trust ACS
    @Value("${app.truststore.client.path}")
    private String clientTruststorePathForAcs;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePasswordForAcs;
    @Value("${app.truststore.acs.alias:acs_trusted}")
    private String acsTrustedAlias;

    /**
     * Processes a payment by sending card details to the ACS server
     * for 3D Secure authentication and OTP generation.
     *
     * @param transaction The transaction to process (contains amount and merchant details)
     * @param client The client making the payment (contains card details)
     * @return true if the process succeeded (OTP generated), false otherwise
     */
    public boolean processPayment(TransactionEntity transaction, Client client) {
        try {
            return processPaymentInternal(transaction, client);
        } catch (Exception e) {
            log.error("Error processing payment through ACS: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Internal implementation of payment processing logic
     */
    private boolean processPaymentInternal(TransactionEntity transaction, Client client) throws Exception {
        String cardNumber = "";
        String expirationDate = "";
        var cards = client.getCards();
        if (cards != null && !cards.isEmpty()) {
            var card = cards.get(0);
            cardNumber = card.getCardNumber();
            expirationDate = card.getExpirationDate();
        }

        if (cardNumber == null || cardNumber.isEmpty() || expirationDate == null || expirationDate.isEmpty()) {
            log.error("Missing card details for client: {}", client.getEmail());
            return false;
        }

        // Prepare data
        String dataToSign = prepareDataForSigning(cardNumber, expirationDate, transaction);

        // Sign data
        PrivateKey privateKey = KeystoreUtils.getPrivateKey(
                clientKeystorePath, clientKeystorePassword, clientKeystoreAlias, clientKeyPassword
        );
        String signedData = SignUtils.signData(dataToSign, privateKey);
        String formattedDataForAcs = formatDataForAcs(dataToSign, signedData);

        // Send to ACS
        String acsResponse = sendToACS(formattedDataForAcs);
        log.info("Received response from ACS: {}", acsResponse);

        if (acsResponse == null || acsResponse.startsWith("ERROR:")) {
            log.error("Received null or error response from ACS: {}", acsResponse);
            return false;
        }

        // Process response
        return processAcsResponse(acsResponse);
    }

    /**
     * Prepares data payload for signing
     */
    private String prepareDataForSigning(String cardNumber, String expirationDate, TransactionEntity transaction) {
        return "card=" + cardNumber + "#date=" + expirationDate +
                "#amount=" + transaction.getTransferAmount() +
                "#merchant=" + transaction.getMerchantName();
    }

    /**
     * Formats data for ACS with source and signature
     */
    private String formatDataForAcs(String dataToSign, String signedData) {
        return "source=" + SOURCE + "&data=" + dataToSign + "&signature=" + signedData;
    }

    /**
     * Processes ACS response
     */
    public boolean processAcsResponse(String acsResponse) throws Exception {
        Map<String, String> parsedData = DataUtils.parseData(acsResponse);
        String responseSource = parsedData.get("source");
        String code = parsedData.get("data"); // Should be the OTP
        String signature = parsedData.get("signature");

        if (responseSource == null || !responseSource.equals("acs") || code == null || signature == null) {
            log.error("Invalid response format from ACS: {}", acsResponse);
            return false;
        }

        // Verify ACS signature
        return verifyAcsSignature(code, signature);
    }

    /**
     * Verifies ACS signature
     */
    private boolean verifyAcsSignature(String data, String signature) throws Exception {
        log.debug("AcsService: Attempting to verify ACS signature using alias '{}' from truststore '{}'",
                acsTrustedAlias, clientTruststorePathForAcs);
        Certificate acsCertificate = KeystoreUtils.getCertificate(
                clientTruststorePathForAcs,
                clientTruststorePasswordForAcs,
                acsTrustedAlias
        );
        PublicKey acsPublicKey = acsCertificate.getPublicKey();

        if (!SignUtils.verifySignature(data, signature, acsPublicKey)) {
            log.error("AcsService: Invalid ACS signature received! Data='{}', Signature='{}'", data, signature);
            return false;
        }
        log.info("AcsService: ACS response signature verified successfully.");
        return true;
    }

    /**
     * Establishes a secure connection with the ACS server and sends the message.
     *
     * @param message The formatted message to send to ACS
     * @return The ACS server response or an error message
     */
    private String sendToACS(String message) {
        log.debug("Attempting to connect to ACS on port {} using truststore {}", acsPort, clientTruststorePathForAcs);
        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsPort,
                clientTruststorePathForAcs,
                clientTruststorePasswordForAcs)) {

            log.debug("SSL Socket created for ACS. Connected: {}", acsSocket.isConnected());
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            writer.println(message);
            log.info("Message sent to ACS: {}", message);
            String response = reader.readLine();
            log.info("Raw response received from ACS: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACS on port {}: {}", acsPort, e.getMessage(), e);
            return "ERROR:Communication failed - " + e.getMessage();
        }
    }
}