package grapes.microservices.paymentbackend.server;

import grapes.microservices.paymentbackend.utils.DataUtils; // <-- Import
import grapes.microservices.paymentbackend.utils.KeystoreUtils; // <-- Import
import grapes.microservices.paymentbackend.utils.SignUtils; // <-- Import
import grapes.microservices.paymentbackend.utils.SslUtils;
import jakarta.annotation.PostConstruct; // <-- Import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey; // <-- Import
import java.security.PublicKey; // <-- Import
import java.security.cert.Certificate; // <-- Import
import java.util.Map; // <-- Import
import java.util.concurrent.Executors;

/**
 * Server component that acts as an Acquirer (ACQ).
 * Receives payment verification tokens (OTP) from PaymentService,
 * signs the token request, sends it to ACS Money port for verification,
 * verifies the signed response from ACS, and returns plain ACK/NACK to PaymentService.
 */
@Component
@Slf4j
public class AcqServer implements CommandLineRunner {

    private static final String SOURCE = "acq"; // Source ID for ACQ

    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    // ACQ Keystore/Key info (for signing requests to ACS)
    @Value("${app.keystore.acq.path}")
    private String acqKeystorePath;
    @Value("${app.keystore.acq.password}")
    private String acqKeystorePassword;
    @Value("${app.keystore.acq.key.alias:acq}") // Default alias 'acq'
    private String acqKeyAlias;
    @Value("${app.keystore.acq.key.password}")
    private String acqKeyPassword;

    // Truststore used by ACQ to trust ACS server and verify its response signature
    @Value("${app.truststore.acq.path}") // Truststore ACQ uses
    private String acqTruststorePath;
    @Value("${app.truststore.acq.password}")
    private String acqTruststorePassword;
    @Value("${app.truststore.acs.alias.in.acq:acs_trusted}") // Alias for ACS cert IN ACQ's truststore
    private String acsTrustedAliasInAcq;

    private PrivateKey acqPrivateKey;
    private PublicKey acsPublicKey;

    @PostConstruct
    public void loadKeys() {
        try {
            // Load ACQ's private key for signing outgoing messages
            this.acqPrivateKey = KeystoreUtils.getPrivateKey(
                    acqKeystorePath, acqKeystorePassword, acqKeyAlias, acqKeyPassword
            );
            // Load ACS's public key cert from ACQ's truststore to verify incoming ACS responses
            Certificate acsCertificate = KeystoreUtils.getCertificate(
                    acqTruststorePath, acqTruststorePassword, acsTrustedAliasInAcq
            );
            this.acsPublicKey = acsCertificate.getPublicKey();
            log.info("ACQ private key and ACS public key loaded successfully for AcqServer.");
        } catch (Exception e) {
            log.error("FATAL: Failed to load keys/certificates for AcqServer. Signature/Verification will fail.", e);
            // Consider throwing an exception or setting a flag to prevent server start
            this.acqPrivateKey = null;
            this.acsPublicKey = null;
        }
    }

    @Override
    public void run(String... args) {
        // Ensure keys are loaded before starting server thread
        if (this.acqPrivateKey == null || this.acsPublicKey == null) {
            log.error("AcqServer cannot start because keys could not be loaded.");
            return; // Prevent server start if keys are missing
        }
        Executors.newSingleThreadExecutor().submit(this::startServer);
    }

    private void startServer() {
        log.info("Starting ACQ server on port {}", acqPort);
        // Load ACQ's server certificate/key for the SSL Server Socket
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acqPort, acqKeystorePath, acqKeystorePassword)) {
            log.info("ACQ server listening on port {}", acqPort);
            while (true) {
                try { // Handle accept errors without crashing server
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    log.info("Client connected to ACQ server from {}", clientSocket.getRemoteSocketAddress());
                    // Process each client connection in a new thread
                    Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket));
                } catch (Exception e) {
                    log.error("Error accepting client connection on ACQ server: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Fatal error starting ACQ server: {}", e.getMessage(), e);
        }
    }


    private void handleClientRequest(SSLSocket clientSocket) {
        String verifiedAcsResponseData = "NACK"; // Default to NACK in case of errors
        String token = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestData = reader.readLine(); // Expecting raw OTP token from PaymentService
            log.info("ACQ Server: Received data from client (PaymentService): {}", requestData);

            if (requestData == null || requestData.isEmpty()) {
                log.warn("ACQ Server: Invalid request data received from PaymentService.");
                writer.println("Response from ACQ: NACK - Invalid request"); // Send plain NACK back
                return;
            }

            token = requestData.trim();

            // Basic token format validation
            if (token.length() != 6 || !token.matches("\\d{6}")) {
                log.warn("ACQ Server: Received data does not look like a 6-digit token: '{}'", token);
                writer.println("Response from ACQ: NACK - Invalid token format"); // Send plain NACK back
                return;
            }

            // Sign the token using ACQ's private key
            String signedToken;
            try {
                if (this.acqPrivateKey == null) throw new IllegalStateException("ACQ Private Key not loaded.");
                signedToken = SignUtils.signData(token, this.acqPrivateKey);
            } catch (Exception e) {
                log.error("ACQ Server: Failed to sign token '{}': {}", token, e.getMessage(), e);
                writer.println("Response from ACQ: NACK - Internal signing error"); // Send plain NACK back
                return;
            }

            // Format message for ACS including source, data (token), and signature
            String dataToSend = "source=" + SOURCE + "&data=" + token + "&signature=" + signedToken;
            // Avoid logging sensitive data like full token/signature in production if possible
            log.info("ACQ Server: Forwarding signed message to ACS for token verification (data length: {}, signature length: {}).", token.length(), signedToken.length());

            // Communicate with ACS: send signed request, receive signed response, verify signature
            verifiedAcsResponseData = communicateWithAcs(dataToSend); // Returns verified "ACK" or "NACK"

            log.info("ACQ Server: Verified response data received from ACS: {}", verifiedAcsResponseData);

            // Return the plain text result (verified internally) back to the PaymentService
            writer.println("Response from ACQ: " + verifiedAcsResponseData);

        } catch (Exception e) {
            log.error("ACQ Server: Error handling client request for token '{}': {}", token, e.getMessage(), e);
            // Try to send a plain NACK if the connection is still open
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("Response from ACQ: NACK - Error processing request");
            } catch (Exception ex) {
                log.error("ACQ Server: Error sending error response back to PaymentService: {}", ex.getMessage());
            }
        } finally {
            try {
                if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch(Exception e) {
                log.error("ACQ Server: Error closing PaymentService client socket: {}", e.getMessage());
            }
        }
    }

    /**
     * Communicates securely with the ACS Money port.
     * Sends a signed message (containing the token), receives a signed response (ACK/NACK),
     * verifies the ACS signature, and returns the verified payload ("ACK" or "NACK").
     *
     * @param messageToAcs The signed message to send (e.g., "source=acq&data=TOKEN&signature=...").
     * @return The verified data ("ACK" or "NACK") from ACS, or "NACK" if verification fails or communication error occurs.
     */
    private String communicateWithAcs(String messageToAcs) {
        log.debug("ACQ Server: Sending signed message to ACS Money port {} : {}", acsMoneyPort, messageToAcs); // Debug level for full message

        // Ensure ACS public key is loaded
        if (this.acsPublicKey == null) {
            log.error("ACQ Server: Cannot communicate with ACS, ACS Public Key not loaded.");
            return "NACK";
        }

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsMoneyPort,
                acqTruststorePath, // ACQ uses its truststore to verify ACS server cert
                acqTruststorePassword
        )) {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            // Send the formatted, signed message to ACS
            writer.println(messageToAcs);
            log.info("ACQ Server: Signed message sent to ACS Money port.");

            // Read the raw response from ACS (expected to be signed)
            String acsRawResponse = reader.readLine();
            log.info("ACQ Server: Raw signed response received from ACS: {}", acsRawResponse);

            if (acsRawResponse == null || acsRawResponse.isEmpty()) {
                log.error("ACQ Server: Received empty response from ACS.");
                return "NACK"; // Treat empty response as failure
            }

            // Parse the response from ACS
            Map<String, String> parsedResponse;
            try {
                parsedResponse = DataUtils.parseData(acsRawResponse);
            } catch (Exception e) {
                log.error("ACQ Server: Failed to parse ACS response '{}': {}", acsRawResponse, e.getMessage());
                return "NACK"; // Treat parse error as failure
            }

            String responseSource = parsedResponse.get("source");
            String responseData = parsedResponse.get("data"); // Should be "ACK" or "NACK"
            String responseSignature = parsedResponse.get("signature");

            // Validate the received message format and source
            if (responseSource == null || !responseSource.equals("acs") || responseData == null || responseSignature == null) {
                log.error("ACQ Server: Invalid response format received from ACS: {}", acsRawResponse);
                return "NACK"; // Treat invalid format as failure
            }

            // Verify the signature using ACS's public key
            try {
                if (SignUtils.verifySignature(responseData, responseSignature, this.acsPublicKey)) {
                    log.info("ACQ Server: ACS signature verified successfully for response data: {}", responseData);
                    // Signature is valid, return the actual result data ("ACK" or "NACK")
                    return responseData;
                } else {
                    // Signature is invalid! Log critical error.
                    log.error("ACQ Server: !!! INVALID ACS SIGNATURE received! Data='{}', Signature='{}'", responseData, responseSignature);
                    return "NACK"; // Treat invalid signature as failure
                }
            } catch (Exception e) {
                log.error("ACQ Server: Error during ACS signature verification: {}", e.getMessage(), e);
                return "NACK"; // Treat verification error as failure
            }

        } catch (Exception e) {
            // Catch communication errors (socket connection, read/write issues)
            log.error("ACQ Server: Error communicating with ACS Money port {}: {}", acsMoneyPort, e.getMessage(), e);
            return "NACK"; // Return NACK on any communication failure
        }
    }
}