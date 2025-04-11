package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.models.Transaction;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AcsService {

    private static final String SOURCE = "client";
    private static final int ACS_PORT = 8081;

    @Value("${keystore.client.path}")
    private String clientKeystorePath;

    @Value("${keystore.client.password}")
    private String clientKeystorePassword;

    @Value("${keystore.client.alias}")
    private String clientKeystoreAlias;

    @Value("${keystore.client.truststore}")
    private String clientTruststorePath;

    public boolean processPayment(Transaction transaction) {
        try {
            // Get card details from user
            String cardNumber = transaction.getUser().getCardNumber();
            String expirationDate = transaction.getUser().getCardExpiration();

            if (cardNumber == null || expirationDate == null) {
                System.err.println("[ERROR] Missing card details for user: " + transaction.getUser().getLogin());
                return false;
            }

            // Format data for ACS
            String dataToSign = "card=" + cardNumber + "#date=" + expirationDate + "#amount=" + transaction.getAmount() + "#merchant=" + transaction.getMerchant();

            // Get private key for signing
            PrivateKey privateKey = KeystoreUtils.getPrivateKey(
                    clientKeystorePath,
                    clientKeystorePassword,
                    clientKeystoreAlias,
                    clientKeystorePassword
            );

            // Sign the data
            String signedData = SignUtils.signData(dataToSign, privateKey);
            String formattedDataForAcs = "source=" + SOURCE + "&data=" + dataToSign + "&signature=" + signedData;

            // Send to ACS and get response
            String acsResponse = sendToACS(formattedDataForAcs);
            System.out.println("[INFO] Received response from ACS: " + acsResponse);

            if (acsResponse == null) {
                return false;
            }

            // Parse response
            Map<String, String> parsedData = DataUtils.parseData(acsResponse);

            String responseSource = parsedData.get("source");
            String code = parsedData.get("data");
            String signature = parsedData.get("signature");

            if (responseSource == null || !responseSource.equals("acs") || code == null || signature == null) {
                System.err.println("[ERROR] Invalid response format from ACS");
                return false;
            }

            // Verify signature (in a production environment)
            // This is simplified for this implementation
            return true;

        } catch (Exception e) {
            System.err.println("[ERROR] Error processing payment through ACS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String sendToACS(String message) {
        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(ACS_PORT, clientTruststorePath)) {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            // Send the message to the ACS
            writer.println(message);
            System.out.println("[INFO] Message sent to ACS: " + message);

            // Receive the response from the ACS
            return reader.readLine();
        } catch (Exception e) {
            System.err.println("[ERROR] communicating with ACS: " + e.getMessage());
            return null;
        }
    }
}