package grapes.microservices.paymentbackend.server;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import grapes.microservices.paymentbackend.repositories.ClientRepository;
import grapes.microservices.paymentbackend.repositories.TransactionRepository;
import grapes.microservices.paymentbackend.services.SmsService;
import grapes.microservices.paymentbackend.utils.DataUtils;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;
import grapes.microservices.paymentbackend.utils.SslUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


@Component
@Slf4j
public class AcsServer implements CommandLineRunner {

    // --- Port Configuration ---
    @Value("${app.ports.acs}")
    private int acsPort;
    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    // --- ACS Keystore/Key Configuration ---
    @Value("${app.keystore.path}")
    private String acsKeystorePath;
    @Value("${app.keystore.password}")
    private String acsKeystorePassword;
    @Value("${app.keystore.key.alias:acs}")
    private String acsKeyAlias;
    @Value("${app.keystore.key.password}")
    private String acsKeyPassword;

    // --- Truststore for Client Authentication (Port 8081) ---
    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;
    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;
    @Value("${app.truststore.client.alias:client_trusted}")
    private String clientTrustedAlias;

    // --- Truststore for ACQ Authentication (Port 8083) ---
    @Value("${app.truststore.acs.path}")
    private String acsTruststorePath;
    @Value("${app.truststore.acs.password}")
    private String acsTruststorePassword;
    @Value("${app.truststore.acq.alias:acq_trusted}")
    private String acqTrustedAlias;

    // --- Dependencies ---
    private final ClientRepository clientRepository;
    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;
    private final TransactionRepository transactionRepository;

    // --- Constants ---
    private static final String SOURCE_ACS = "acs";
    private static final String SOURCE_CLIENT = "client";
    private static final String SOURCE_ACQ = "acq";

    // --- Loaded Keys ---
    private PrivateKey acsPrivateKey;
    private PublicKey clientPublicKey;
    private PublicKey acqPublicKey;

    // --- Other state (if needed) ---
    private final Map<Long, Integer> failedAttempts = new ConcurrentHashMap<>();

    @Autowired
    public AcsServer(ClientRepository clientRepository,
                     AuthTokenRepository tokenRepository,
                     SmsService smsService,
                     TransactionRepository transactionRepository) {
        this.clientRepository = clientRepository;
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void loadKeys() {
        try {
            this.acsPrivateKey = KeystoreUtils.getPrivateKey(
                    acsKeystorePath, acsKeystorePassword, acsKeyAlias, acsKeyPassword
            );

            log.debug("Loading Client public key using alias '{}' from ACS truststore '{}'", clientTrustedAlias, acsTruststorePath);
            Certificate clientCertificate = KeystoreUtils.getCertificate(
                    acsTruststorePath, acsTruststorePassword, clientTrustedAlias
            );
            if (clientCertificate == null) throw new RuntimeException("Client certificate with alias '" + clientTrustedAlias + "' not found in ACS truststore: " + acsTruststorePath);
            this.clientPublicKey = clientCertificate.getPublicKey();

            log.debug("Loading ACQ public key using alias '{}' from ACS truststore '{}'", acqTrustedAlias, acsTruststorePath);
            Certificate acqCertificate = KeystoreUtils.getCertificate(
                    acsTruststorePath, acsTruststorePassword, acqTrustedAlias
            );
            if (acqCertificate == null) throw new RuntimeException("ACQ certificate with alias '" + acqTrustedAlias + "' not found in ACS truststore: " + acsTruststorePath);
            this.acqPublicKey = acqCertificate.getPublicKey();

            log.info("ACS keys (private ACS, public Client, public ACQ) loaded successfully for AcsServer.");

        } catch (Exception e) {
            log.error("FATAL: Failed to load keys/certificates for AcsServer. Signature/Verification will fail.", e);
            this.acsPrivateKey = null;
            this.clientPublicKey = null;
            this.acqPublicKey = null;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) {
        if (this.acsPrivateKey == null || this.clientPublicKey == null || this.acqPublicKey == null) {
            log.error("AcsServer cannot start because keys could not be loaded.");
            return;
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::startMainServer);
        executorService.submit(this::startMoneyServer);
    }

    // --- startMainServer --- (Unchanged from previous version)
    private void startMainServer() {
        log.info("Attempting to start ACS main server on port {}", acsPort);
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsPort, acsKeystorePath, acsKeystorePassword)) {
            log.info("ACS main server successfully listening on port {}", acsPort);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    log.info("ACS main server accepted connection from {}", clientSocket.getRemoteSocketAddress());
                    Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket));
                } catch (Exception e) {
                    log.error("Error accepting connection on ACS main port {}: {}", acsPort, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Fatal error starting ACS main server on port {}: {}", acsPort, e.getMessage(), e);
        }
    }

    // --- startMoneyServer --- (Unchanged from previous version)
    private void startMoneyServer() {
        log.info("Attempting to start ACS Money server on port {}", acsMoneyPort);
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsMoneyPort, acsKeystorePath, acsKeystorePassword)) {
            log.info("ACS Money server successfully listening on port {}", acsMoneyPort);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket acqSocket = (SSLSocket) serverSocket.accept();
                    log.info("ACS Money server accepted connection from {}", acqSocket.getRemoteSocketAddress());
                    Executors.newSingleThreadExecutor().submit(() -> handleVerificationRequest(acqSocket));
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
     * (Unchanged from previous version)
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String rawData = reader.readLine();
            log.debug("ACS Main (8081): Received raw data: {}", rawData);

            if (rawData == null || rawData.isEmpty()) {
                log.warn("ACS Main (8081): No data received from client.");
                return;
            }

            Map<String, String> parsedData;
            try {
                parsedData = DataUtils.parseData(rawData);
            } catch (Exception e) {
                log.warn("ACS Main (8081): Failed to parse client request '{}': {}", rawData, e.getMessage());
                sendSignedErrorResponse(writer, "ERROR:Invalid data format.");
                return;
            }

            String source = parsedData.get("source");
            String clientPayload = parsedData.get("data");
            String clientSignature = parsedData.get("signature");

            if (source == null || clientPayload == null || clientSignature == null || !source.equals(SOURCE_CLIENT)) {
                log.warn("ACS Main (8081): Invalid data format or source. Received: {}", rawData);
                sendSignedErrorResponse(writer, "ERROR:Invalid data format or source.");
                return;
            }
            log.info("ACS Main (8081): Processing request from source: {}", source);

            boolean clientSigVerified = false;
            try {
                if (this.clientPublicKey == null) throw new IllegalStateException("Client Public Key not loaded.");
                clientSigVerified = SignUtils.verifySignature(clientPayload, clientSignature, this.clientPublicKey);
            } catch (Exception e) {
                log.error("ACS Main (8081): Error verifying client signature: {}", e.getMessage(), e);
                sendSignedErrorResponse(writer, "ERROR:Signature verification failed.");
                return;
            }

            if (!clientSigVerified) {
                log.warn("ACS Main (8081): Invalid client signature for data: {}", clientPayload);
                sendSignedErrorResponse(writer, "ERROR:Invalid signature.");
                return;
            }

            log.info("ACS Main (8081): Client signature verified successfully.");
            Map<String, String> cardDetailsMap;
            try {
                // Using helper to parse inner data payload "key=value#key2=value2"
                cardDetailsMap = parseInnerData(clientPayload);
            } catch (IllegalArgumentException e) {
                log.warn("ACS Main (8081): Malformed data payload: '{}'. Error: {}", clientPayload, e.getMessage());
                sendSignedErrorResponse(writer, "ERROR:Invalid data payload format.");
                return;
            }

            String cardNumber = cardDetailsMap.get("card");
            if (cardNumber == null) {
                log.warn("ACS Main (8081): Card number missing in data: {}", clientPayload);
                sendSignedErrorResponse(writer, "ERROR:Card number missing.");
                return;
            }
            log.info("ACS Main (8081): Received card number (masked): {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));

            // Find client associated with card
            Optional<Client> clientOpt = findClientByCard(cardNumber);

            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                log.info("ACS Main (8081): Found client {} associated with the request", client.getEmail());
                String otpCode = generateOtpCode();
                log.info("ACS Main (8081): Generated OTP code: {} for client {}", otpCode, client.getEmail());

                // Save the token (associating it with the client)
                try {
                    AuthToken token = new AuthToken(otpCode, client);
                    // Ideally, associate transactionId here if passed in clientPayload
                    // token.setTransactionId(Long.parseLong(cardDetailsMap.get("transactionId")));
                    tokenRepository.save(token);
                    log.info("ACS Main (8081): Saved AuthToken ID {} for client {}", token.getId(), client.getEmail());
                } catch (Exception e) {
                    log.error("ACS Main (8081): FAILED to save AuthToken for client {}: {}", client.getEmail(), e.getMessage(), e);
                    sendSignedErrorResponse(writer, "ERROR:Failed to process token generation.");
                    return;
                }

                // Send OTP via SMS
                try {
                    log.info("ACS Main (8081): Sending OTP '{}' via SMS to {}", otpCode, client.getPhoneNumber());
                    smsService.sendOtp(client.getPhoneNumber(), otpCode);
                } catch (RuntimeException e) {
                    log.error("ACS Main (8081): Failed to send SMS OTP to client {}: {}", client.getEmail(), e.getMessage());
                    sendSignedErrorResponse(writer, "ERROR:Failed to send OTP.");
                    return;
                }

                // Send signed response (containing the OTP) back to the backend
                sendSignedResponse(writer, otpCode);
                log.info("ACS Main (8081): Sent SIGNED response to client with OTP.");

            } else {
                log.warn("ACS Main (8081): No client found for card: {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));
                sendSignedErrorResponse(writer, "ERROR:Client not found for this card.");
            }

        } catch (Exception e) {
            log.error("ACS Main (8081): Unhandled error handling client request: {}", e.getMessage(), e);
        } finally {
            try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (Exception e) { log.error("ACS Main (8081): Error closing client socket: {}", e.getMessage()); }
        }
    }

    /**
     * Handles SIGNED OTP verification requests from the Acquirer (ACQ) server on the Money Port (8083).
     * Verifies ACQ signature (covering token AND transactionId), validates token against database
     * (exists, valid, not used, belongs to the client matching the transactionId),
     * and returns a SIGNED "ACK" or "NACK". Marks token as used ONLY on successful validation.
     */
    @Transactional // Important for atomic read-check-update
    protected void handleVerificationRequest(SSLSocket acqSocket) {
        String result = "NACK"; // Default result
        String tokenValue = null;
        Long transactionId = null;
        String dataPayloadFromAcq = null; // For logging and signature verification

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(acqSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true)) {

            String receivedData = reader.readLine();
            log.info("ACS Money (8083): Received raw data from ACQ: '{}'", receivedData);

            if (receivedData == null || receivedData.isEmpty()) {
                log.warn("ACS Money (8083): No data received from ACQ.");
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // Parse message: source=acq&data=token=...#txId=...&signature=...
            Map<String, String> parsedData;
            try {
                parsedData = DataUtils.parseData(receivedData);
            } catch (Exception e) {
                log.error("ACS Money (8083): Failed to parse ACQ request '{}': {}", receivedData, e.getMessage());
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            String source = parsedData.get("source");
            dataPayloadFromAcq = parsedData.get("data"); // Ex: "token=123456#txId=9876543210"
            String acqSignature = parsedData.get("signature");

            // Validate format and source basics
            if (source == null || !source.equals(SOURCE_ACQ) || dataPayloadFromAcq == null || acqSignature == null || !dataPayloadFromAcq.contains("#") || !dataPayloadFromAcq.contains("=")) {
                log.warn("ACS Money (8083): Invalid data format or source from ACQ. Received: {}", receivedData);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // --- Verify ACQ Signature (on the dataPayloadFromAcq) ---
            boolean acqSigVerified = false;
            try {
                if (this.acqPublicKey == null) throw new IllegalStateException("ACQ Public Key not loaded.");
                // IMPORTANT: Verify signature on the data payload received from ACQ
                acqSigVerified = SignUtils.verifySignature(dataPayloadFromAcq, acqSignature, this.acqPublicKey);
            } catch (Exception e) {
                log.error("ACS Money (8083): Error verifying ACQ signature: {}", e.getMessage(), e);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return; // Stop if error during verification
            }

            if (!acqSigVerified) {
                log.error("ACS Money (8083): !!! INVALID ACQ SIGNATURE received! Data='{}', Signature='{}'", dataPayloadFromAcq, acqSignature);
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return; // Stop if signature is invalid
            }
            log.info("ACS Money (8083): ACQ signature verified successfully for data: {}", dataPayloadFromAcq);

            // --- Parse dataPayloadFromAcq: token=...#txId=... ---
            try {
                // Use helper to parse the inner key-value pairs separated by #
                Map<String, String> innerData = parseInnerData(dataPayloadFromAcq);
                tokenValue = innerData.get("token");
                String transactionIdStr = innerData.get("txId");

                if (tokenValue == null || transactionIdStr == null) {
                    throw new IllegalArgumentException("Missing token or txId in data payload");
                }
                // Optional: Validate format further
                if (tokenValue.length() != 6 || !tokenValue.matches("\\d{6}") || !transactionIdStr.matches("\\d+")) {
                    throw new IllegalArgumentException("Invalid token or txId format");
                }
                transactionId = Long.parseLong(transactionIdStr);

            } catch (Exception e) { // Catch parsing errors (NumberFormat, IllegalArgument)
                log.error("ACS Money (8083): Failed to parse inner data payload '{}': {}", dataPayloadFromAcq, e.getMessage());
                sendSignedResponse(writer, "NACK"); // Send signed NACK
                return;
            }

            // --- Comprehensive Validation ---
            log.info("ACS Money (8083): Performing comprehensive validation for token '{}' and transaction '{}'", tokenValue, transactionId);
            Optional<AuthToken> tokenOpt = tokenRepository.findByToken(tokenValue);
            // Use the injected repository to find the transaction
            Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);

            if (tokenOpt.isPresent() && transactionOpt.isPresent()) {
                AuthToken authToken = tokenOpt.get();
                TransactionEntity transactionEntity = transactionOpt.get();
                log.info("ACS Money (8083): Found token ID {} (Client {}) and transaction ID {} (Client {})",
                        authToken.getId(), authToken.getClient().getId(),
                        transactionEntity.getId(), transactionEntity.getClientId());

                // Check 1: Token validity (not used, not expired)
                // Check 2: Client match between token owner and transaction owner
                if (authToken.isValid() && authToken.getClient().getId().equals(transactionEntity.getClientId())) {

                    log.info("ACS Money (8083): Token is valid and client match successful (Client ID: {}). Marking token as used.", authToken.getClient().getId());
                    // Validation Successful! Mark token as used and set result to ACK.
                    authToken.setUsed(true);
                    tokenRepository.save(authToken); // Changes will be committed by @Transactional
                    result = "ACK";

                } else {
                    // Log specific reason for NACK
                    if (!authToken.isValid()) {
                        // Log if token is used or expired
                        log.warn("ACS Money (8083): NACK reason: Token ID {} invalid (Used={} or Expired={}). Tx ID: {}",
                                authToken.getId(), authToken.isUsed(), LocalDateTime.now().isAfter(authToken.getExpiresAt()), transactionId);
                    } else { // If token was valid, the only other failure reason is client mismatch
                        log.warn("ACS Money (8083): NACK reason: Client mismatch! Token client ID: {}, Transaction client ID: {}. Tx ID: {}",
                                authToken.getClient().getId(), transactionEntity.getClientId(), transactionId);
                    }
                    // Do NOT mark token as used if validation fails
                    result = "NACK";
                }
            } else {
                // Log specific reason for NACK if token or transaction not found
                if (tokenOpt.isEmpty()) log.warn("ACS Money (8083): NACK reason: Token '{}' not found. Tx ID: {}", tokenValue, transactionId);
                if (transactionOpt.isEmpty()) log.warn("ACS Money (8083): NACK reason: Transaction '{}' not found. Token: {}", transactionId, tokenValue);
                result = "NACK";
            }

            // Send the SIGNED result ("ACK" or "NACK") back to ACQ
            sendSignedResponse(writer, result);

        } catch (Exception e) {
            log.error("ACS Money (8083): Unhandled error handling verification request for token '{}', txId '{}': {}", tokenValue, transactionId, e.getMessage(), e);
            // Attempt to send signed NACK
            try (PrintWriter writer = new PrintWriter(acqSocket.getOutputStream(), true)) {
                sendSignedResponse(writer, "NACK");
            } catch (Exception ex) {
                log.error("ACS Money (8083): Error sending signed NACK response after exception: {}", ex.getMessage());
            }
        } finally {
            try { if (acqSocket != null && !acqSocket.isClosed()) acqSocket.close(); } catch (Exception e) { log.error("ACS Money (8083): Error closing ACQ socket: {}", e.getMessage()); }
        }
    }

    /**
     * Helper method to parse inner data payload like "key1=value1#key2=value2".
     * @param data The string data to parse.
     * @return A Map of key-value pairs.
     * @throws IllegalArgumentException if parsing fails or format is incorrect.
     */
    private Map<String, String> parseInnerData(String data) throws IllegalArgumentException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Inner data payload cannot be null or empty");
        }
        Map<String, String> dataMap = new HashMap<>();
        String[] pairs = data.split("#"); // Split by #
        if (pairs.length == 0) {
            throw new IllegalArgumentException("No pairs found in inner data payload: " + data);
        }
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2); // Split by =
            if (keyValue.length == 2 && !keyValue[0].isEmpty() && !keyValue[1].isEmpty()) {
                dataMap.put(keyValue[0], keyValue[1]);
            } else {
                throw new IllegalArgumentException("Malformed pair found in inner data payload: " + pair);
            }
        }
        return dataMap;
    }


    // --- Helper Methods (findClientByCard, generateOtpCode, sendSignedResponse, sendSignedErrorResponse) ---

    private Optional<Client> findClientByCard(String cardNumber) {
        // Find client by card number using repository
        Optional<Client> clientOpt = clientRepository.findByCards_CardNumber(cardNumber);
        // Fallback logic (Consider removing for production)
        if (clientOpt.isEmpty()) {
            log.warn("ACS: No client found for card number: {}. Trying alternative method.", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));
            clientOpt = clientRepository.findAll().stream()
                    .filter(client -> client.getPhoneNumber() != null && !client.getPhoneNumber().isEmpty())
                    .findFirst();
            if (clientOpt.isPresent()) log.warn("ACS: Using fallback method to find client. NOT FOR PRODUCTION!");
        }
        return clientOpt;
    }

    private String generateOtpCode() {
        // Generate a 6-digit OTP
        Random random = new Random();
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
            // Avoid sending unsigned response on signing failure
        }
    }

    /** Helper method to send SIGNED ERROR responses (uses same mechanism). */
    private void sendSignedErrorResponse(PrintWriter writer, String errorMessage) {
        // Send error message signed by ACS
        sendSignedResponse(writer, errorMessage);
    }
}