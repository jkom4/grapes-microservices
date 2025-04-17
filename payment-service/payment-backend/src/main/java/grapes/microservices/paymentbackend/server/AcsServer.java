package grapes.microservices.paymentbackend.server;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import grapes.microservices.paymentbackend.services.SmsService;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import jakarta.annotation.PostConstruct; // <-- Import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Keep or add

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey; // <-- Import
import java.security.cert.Certificate; // <-- Import
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Authentication Server (ACS) component handles OTP generation/delivery (main port)
 * and signed OTP verification requests from ACQ (money port).
 */
@Component
@Slf4j
public class AcsServer implements CommandLineRunner {

    // --- Port Configuration ---
    @Value("${app.ports.acs}")
    private int acsPort; // Main port for client communication (e.g., OTP generation)
    @Value("${app.ports.acs.money}")
    private int acsMoneyPort; // Port for ACQ communication (e.g., OTP verification)

    // --- ACS Keystore/Key Configuration (ACS's own private key) ---
    @Value("${app.keystore.path}")
    private String acsKeystorePath;
    @Value("${app.keystore.password}")
    private String acsKeystorePassword;
    @Value("${app.keystore.key.alias:acs}") // Default 'acs'
    private String acsKeyAlias;
    @Value("${app.keystore.key.password}")
    private String acsKeyPassword;

    // --- Truststore for Client Authentication (Used on Main Port 8081) ---
    // ACS needs client's public key to verify requests on main port
    @Value("${app.truststore.client.path}") // Where ACS finds trusted client certs
    private String clientTruststorePath;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;
    @Value("${app.truststore.client.alias:client_trusted}") // Alias for Client cert IN ACS's view
    private String clientTrustedAlias;

    // --- Truststore for ACQ Authentication (Used on Money Port 8083) ---
    // ACS needs ACQ's public key to verify requests on money port
    @Value("${app.truststore.acs.path}") // ACS uses its own truststore path here
    private String acsTruststorePath;
    @Value("${app.truststore.acs.password}")
    private String acsTruststorePassword;
    @Value("${app.truststore.acq.alias:acq_trusted}") // Alias for ACQ cert IN ACS's truststore
    private String acqTrustedAlias;

    // --- Dependencies ---
    private final ClientRepository clientRepository;
    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    // --- Constants ---
    private static final String SOURCE_ACS = "acs";
    private static final String SOURCE_CLIENT = "client";
    private static final String SOURCE_ACQ = "acq";

    // --- Loaded Keys ---
    private PrivateKey acsPrivateKey;   // ACS uses this to sign responses
    private PublicKey clientPublicKey;  // ACS uses this to verify client requests (port 8081)
    private PublicKey acqPublicKey;     // ACS uses this to verify ACQ requests (port 8083)

    // --- Other state (if needed) ---
    private final Map<Long, Integer> failedAttempts = new ConcurrentHashMap<>(); // Example state

    @Autowired
    public AcsServer(ClientRepository clientRepository, AuthTokenRepository tokenRepository, SmsService smsService) {
        this.clientRepository = clientRepository;
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
    }

    @PostConstruct
    public void loadKeys() {
        try {
            // Load ACS's private key (Correct)
            this.acsPrivateKey = KeystoreUtils.getPrivateKey(
                    acsKeystorePath, acsKeystorePassword, acsKeyAlias, acsKeyPassword
            );

            // --- MODIFICATION ICI ---
            // Load Client's public key (from ACS's perspective/truststore for clients)
            log.debug("Loading Client public key using alias '{}' from ACS truststore '{}'", clientTrustedAlias, acsTruststorePath);
            Certificate clientCertificate = KeystoreUtils.getCertificate(
                    acsTruststorePath,
                    acsTruststorePassword,
                    clientTrustedAlias
            );
            if (clientCertificate == null) {
                throw new RuntimeException("Client certificate with alias '" + clientTrustedAlias + "' not found in ACS truststore: " + acsTruststorePath);
            }
            this.clientPublicKey = clientCertificate.getPublicKey();

            log.debug("Loading ACQ public key using alias '{}' from ACS truststore '{}'", acqTrustedAlias, acsTruststorePath);
            Certificate acqCertificate = KeystoreUtils.getCertificate(
                    acsTruststorePath,
                    acsTruststorePassword,
                    acqTrustedAlias
            );
            if (acqCertificate == null) {
                throw new RuntimeException("ACQ certificate with alias '" + acqTrustedAlias + "' not found in ACS truststore: " + acsTruststorePath);
            }
            this.acqPublicKey = acqCertificate.getPublicKey();

            log.info("ACS keys (private ACS, public Client, public ACQ) loaded successfully for AcsServer.");

        } catch (Exception e) {
            log.error("FATAL: Failed to load keys/certificates for AcsServer. Signature/Verification will fail.", e);
            // Set keys to null to prevent server start if loading fails
            this.acsPrivateKey = null;
            this.clientPublicKey = null;
            this.acqPublicKey = null;
            // Rethrow or handle differently if needed, prevents server start via run() method check
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) {
        // Prevent startup if critical keys failed to load
        if (this.acsPrivateKey == null || this.clientPublicKey == null || this.acqPublicKey == null) {
            log.error("AcsServer cannot start because keys could not be loaded.");
            return;
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::startMainServer); // Handles client requests (port 8081)
        executorService.submit(this::startMoneyServer); // Handles ACQ requests (port 8083)
    }

    // --- startMainServer: Listens on acsPort (8081), uses handleClientRequest ---
    private void startMainServer() {
        log.info("Attempting to start ACS main server on port {}", acsPort);
        // Uses ACS's keystore for its server identity
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsPort, acsKeystorePath, acsKeystorePassword)) {
            log.info("ACS main server successfully listening on port {}", acsPort);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    log.info("ACS main server accepted connection from {}", clientSocket.getRemoteSocketAddress());
                    Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket)); // Uses CLIENT handler
                } catch (Exception e) {
                    log.error("Error accepting connection on ACS main port {}: {}", acsPort, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Fatal error starting ACS main server on port {}: {}", acsPort, e.getMessage(), e);
        }
    }

    // --- startMoneyServer: Listens on acsMoneyPort (8083), uses handleVerificationRequest ---
    private void startMoneyServer() {
        log.info("Attempting to start ACS Money server on port {}", acsMoneyPort);
        // Uses ACS's keystore for its server identity
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsMoneyPort, acsKeystorePath, acsKeystorePassword)) {
            log.info("ACS Money server successfully listening on port {}", acsMoneyPort);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket acqSocket = (SSLSocket) serverSocket.accept();
                    log.info("ACS Money server accepted connection from {}", acqSocket.getRemoteSocketAddress());
                    Executors.newSingleThreadExecutor().submit(() -> handleVerificationRequest(acqSocket)); // Uses ACQ handler
                } catch (Exception e) {
                    log.error("Error accepting connection on ACS Money port {}: {}", acsMoneyPort, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Fatal error starting ACS Money server on port {}: {}", acsMoneyPort, e.getMessage(), e);
        }
    }


    /**
     * Handles SIGNED client payment initiation requests on the main port (8081).
     * Verifies client signature, identifies client, generates OTP, saves token, sends SMS,
     * and returns SIGNED OTP response.
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String rawData = reader.readLine();
            log.debug("ACS Main (8081): Received raw data: {}", rawData);

            if (rawData == null || rawData.isEmpty()) {
                log.warn("ACS Main (8081): No data received from client.");
                // No response needed or send unsigned error? Decided not to respond on empty.
                return;
            }

            // Parse incoming message
            Map<String, String> parsedData;
            try {
                parsedData = DataUtils.parseData(rawData);
            } catch (Exception e) {
                log.warn("ACS Main (8081): Failed to parse client request '{}': {}", rawData, e.getMessage());
                sendSignedErrorResponse(writer, "ERROR:Invalid data format."); // Send signed error
                return;
            }

            String source = parsedData.get("source");
            String clientPayload = parsedData.get("data"); // card details etc.
            String clientSignature = parsedData.get("signature");

            // Validate format and source
            if (source == null || clientPayload == null || clientSignature == null || !source.equals(SOURCE_CLIENT)) {
                log.warn("ACS Main (8081): Invalid data format or source. Received: {}", rawData);
                sendSignedErrorResponse(writer, "ERROR:Invalid data format or source.");
                return;
            }
            log.info("ACS Main (8081): Processing request from source: {}", source);

            // Verify Client Signature using Client's public key
            boolean clientSigVerified = false;
            try {
                if (this.clientPublicKey == null) throw new IllegalStateException("Client Public Key not loaded.");
                clientSigVerified = SignUtils.verifySignature(clientPayload, clientSignature, this.clientPublicKey);
            } catch (Exception e) {
                log.error("ACS Main (8081): Error verifying client signature: {}", e.getMessage(), e);
                // Don't reveal specific error, send generic signed error
                sendSignedErrorResponse(writer, "ERROR:Signature verification failed.");
                return; // Stop processing if verification fails
            }

            if (!clientSigVerified) {
                log.warn("ACS Main (8081): Invalid client signature for data: {}", clientPayload);
                sendSignedErrorResponse(writer, "ERROR:Invalid signature.");
                return; // Stop processing
            }

            // --- If signature is valid, proceed ---
            log.info("ACS Main (8081): Client signature verified successfully.");
            Map<String, String> cardDetailsMap = DataUtils.parseData(clientPayload.replace("#", "&"));
            String cardNumber = cardDetailsMap.get("card");
            if (cardNumber == null) {
                log.warn("ACS Main (8081): Card number missing in data: {}", clientPayload);
                sendSignedErrorResponse(writer, "ERROR:Card number missing.");
                return;
            }
            log.info("ACS Main (8081): Received card number (masked): {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));

            Optional<Client> clientOpt = findClientByCard(cardNumber); // Use helper

            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                log.info("ACS Main (8081): Found client {} associated with the request", client.getEmail());
                String otpCode = generateOtpCode();
                log.info("ACS Main (8081): Generated OTP code: {} for client {}", otpCode, client.getEmail());

                // Save token (handle potential DB errors)
                try {
                    AuthToken token = new AuthToken(otpCode, client);
                    tokenRepository.save(token);
                    log.info("ACS Main (8081): Saved AuthToken ID {} for client {}", token.getId(), client.getEmail());
                } catch (Exception e) {
                    log.error("ACS Main (8081): FAILED to save AuthToken for client {}: {}", client.getEmail(), e.getMessage(), e);
                    sendSignedErrorResponse(writer, "ERROR:Failed to process token generation.");
                    return;
                }

                // Send SMS (handle potential SMS errors)
                try {
                    log.info("ACS Main (8081): Sending OTP '{}' via SMS to {}", otpCode, client.getPhoneNumber());
                    smsService.sendOtp(client.getPhoneNumber(), otpCode);
                } catch (RuntimeException e) {
                    log.error("ACS Main (8081): Failed to send SMS OTP to client {}: {}", client.getEmail(), e.getMessage());
                    // Decide if this is critical? Maybe proceed but log warning?
                    // For now, treat as error and send signed error response
                    sendSignedErrorResponse(writer, "ERROR:Failed to send OTP.");
                    return;
                }

                // Send SIGNED response back to client
                sendSignedResponse(writer, otpCode); // Send the OTP as signed data
                log.info("ACS Main (8081): Sent SIGNED response to client with OTP.");

            } else {
                log.warn("ACS Main (8081): No client found for card: {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));
                sendSignedErrorResponse(writer, "ERROR:Client not found for this card.");
            }

        } catch (Exception e) {
            log.error("ACS Main (8081): Unhandled error handling client request: {}", e.getMessage(), e);
            // Cannot reliably send signed error here if basic IO failed
        } finally {
            try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (Exception e) { log.error("ACS Main (8081): Error closing client socket: {}", e.getMessage()); }
        }
    }


    /**
     * Handles SIGNED OTP verification requests from the Acquirer (ACQ) server on the Money Port (8083).
     * Verifies ACQ signature, validates token against database, and returns a SIGNED "ACK" or "NACK".
     */
    @Transactional // Ensure atomic token read and update (setUsed)
    protected void handleVerificationRequest(SSLSocket acqSocket) {
        String result = "NACK"; // Default result
        String tokenValue = null; // To log the token value in case of error

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true)) {

            String receivedData = reader.readLine();
            log.info("ACS Money (8083): Received raw data from ACQ: '{}'", receivedData);

            if (receivedData == null || receivedData.isEmpty()) {
                log.warn("ACS Money (8083): No data received from ACQ.");
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // Parse the formatted message from ACQ
            Map<String, String> parsedData;
            try {
                parsedData = DataUtils.parseData(receivedData);
            } catch (Exception e) {
                log.error("ACS Money (8083): Failed to parse ACQ request '{}': {}", receivedData, e.getMessage());
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            String source = parsedData.get("source");
            tokenValue = parsedData.get("data"); // This is the OTP token submitted by user via ACQ
            String acqSignature = parsedData.get("signature");

            // Validate format and source
            if (source == null || !source.equals(SOURCE_ACQ) || tokenValue == null || acqSignature == null) {
                log.warn("ACS Money (8083): Invalid data format or source from ACQ. Received: {}", receivedData);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // Verify ACQ Signature using ACQ's public key
            boolean acqSigVerified = false;
            try {
                if (this.acqPublicKey == null) throw new IllegalStateException("ACQ Public Key not loaded.");
                acqSigVerified = SignUtils.verifySignature(tokenValue, acqSignature, this.acqPublicKey);
            } catch (Exception e) {
                log.error("ACS Money (8083): Error verifying ACQ signature: {}", e.getMessage(), e);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return; // Stop if error during verification
            }

            if (!acqSigVerified) {
                log.error("ACS Money (8083): !!! INVALID ACQ SIGNATURE received! Token='{}', Signature='{}'", tokenValue, acqSignature);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return; // Stop if signature is invalid
            }

            // --- If ACQ signature is valid, proceed ---
            log.info("ACS Money (8083): ACQ signature verified successfully for token '{}'.", tokenValue);

            // Basic token format check
            if (tokenValue.length() != 6 || !tokenValue.matches("\\d{6}")) {
                log.warn("ACS Money (8083): Invalid token format received from ACQ (post-signature check): '{}'", tokenValue);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // Proceed with token validation from database
            log.info("ACS Money (8083): Searching for token '{}' in the database.", tokenValue);
            Optional<AuthToken> tokenOpt = tokenRepository.findByToken(tokenValue);
            log.info("ACS Money (8083): Token lookup result for '{}': Found={}", tokenValue, tokenOpt.isPresent());

            if (tokenOpt.isPresent()) {
                AuthToken authToken = tokenOpt.get();
                log.info("ACS Money (8083): Token ID {} found. Checking validity: isUsed={}, expiresAt={}, Now={}",
                        authToken.getId(), authToken.isUsed(), authToken.getExpiresAt(), LocalDateTime.now());

                if (authToken.isValid()) {
                    // Token is valid - mark as used within the transaction
                    authToken.setUsed(true);
                    tokenRepository.save(authToken); // Changes will be committed by @Transactional
                    log.info("ACS Money (8083): Token ID {} marked as used.", authToken.getId());
                    result = "ACK"; // Set result to ACK
                } else {
                    // Token is found but invalid (already used or expired)
                    log.warn("ACS Money (8083): Token ID {} found but is invalid (Used={} or Expired={}).",
                            authToken.getId(), authToken.isUsed(), LocalDateTime.now().isAfter(authToken.getExpiresAt()));
                    result = "NACK";
                }
            } else {
                // Token not found in database
                log.warn("ACS Money (8083): Token '{}' not found in database.", tokenValue);
                result = "NACK";
            }

            // Send the SIGNED result ("ACK" or "NACK") back to ACQ
            sendSignedResponse(writer, result);

        } catch (Exception e) {
            log.error("ACS Money (8083): Unhandled error handling verification request for token '{}': {}", tokenValue, e.getMessage(), e);
            // Attempt to send a signed NACK if possible
            try (PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true)) {
                sendSignedResponse(writer, "NACK");
            } catch (Exception ex) {
                log.error("ACS Money (8083): Error sending signed NACK response after exception: {}", ex.getMessage());
            }
        } finally {
            try { if (acqSocket != null && !acqSocket.isClosed()) acqSocket.close(); } catch (Exception e) { log.error("ACS Money (8083): Error closing ACQ socket: {}", e.getMessage()); }
        }
    }

    // --- Helper Methods ---

    private Optional<Client> findClientByCard(String cardNumber) {
        Optional<Client> clientOpt = clientRepository.findByCards_CardNumber(cardNumber);
        // Fallback logic (consider removing for production)
        if (!clientOpt.isPresent()) {
            log.warn("ACS: No client found for card number: {}. Trying alternative method.", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));
            clientOpt = clientRepository.findAll().stream()
                    .filter(client -> client.getPhoneNumber() != null && !client.getPhoneNumber().isEmpty())
                    .findFirst();
            if (clientOpt.isPresent()) log.warn("ACS: Using fallback method to find client. NOT FOR PRODUCTION!");
        }
        return clientOpt;
    }

    private String generateOtpCode() {
        Random random = new Random();
        // Ensure positive number and pad with leading zeros if necessary
        return String.format("%06d", random.nextInt(1000000));
    }

    /** Helper method to send SIGNED responses back to the caller (Client or ACQ). */
    private void sendSignedResponse(PrintWriter writer, String dataPayload) {
        try {
            if (this.acsPrivateKey == null) throw new IllegalStateException("ACS Private Key not loaded.");
            String signature = SignUtils.signData(dataPayload, this.acsPrivateKey);
            String response = "source=" + SOURCE_ACS + "&data=" + dataPayload + "&signature=" + signature;
            writer.println(response);
            log.info("ACS: Sent signed response with data: {}", dataPayload);
            log.debug("ACS: Full signed response: {}", response);
        } catch (Exception e) {
            log.error("ACS: Failed to sign or send response for data '{}': {}", dataPayload, e.getMessage(), e);
            // Avoid sending unsigned response on signing failure, just log.
        }
    }

    /** Helper method to send SIGNED ERROR responses (uses same mechanism). */
    private void sendSignedErrorResponse(PrintWriter writer, String errorMessage) {
        // We sign error messages too, so the receiver knows it's a genuine error from ACS
        sendSignedResponse(writer, errorMessage);
    }
}