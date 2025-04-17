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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Authentication Server (ACS) component that handles:
 * 1. OTP generation and delivery during payment initiation
 * 2. Token verification during payment completion
 *
 * Runs two separate SSL servers: the main server for payment initiation
 * and the money server for OTP verification from the Acquirer.
 */
@Component
@Slf4j
public class AcsServer implements CommandLineRunner {

    @Value("${app.ports.acs}")
    private int acsPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    // ACS server keystore
    @Value("${app.keystore.path}")
    private String acsKeystorePath;

    @Value("${app.keystore.password}")
    private String acsKeystorePassword;

    // ACS server private key
    @Value("${app.keystore.key.alias}")
    private String acsKeyAlias;

    @Value("${app.keystore.key.password}")
    private String acsKeyPassword;

    // Truststore for client authentication
    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;

    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;

    private final ClientRepository clientRepository;
    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    private static final String SOURCE = "acs";

    // Map to track failed authentication attempts
    private final Map<Long, Integer> failedAttempts = new ConcurrentHashMap<>();

    @Autowired
    public AcsServer(ClientRepository clientRepository, AuthTokenRepository tokenRepository, SmsService smsService) {
        this.clientRepository = clientRepository;
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
    }

    /**
     * Starts both ACS servers in separate threads when application launches.
     */
    @Override
    public void run(String... args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::startMainServer);
        executorService.submit(this::startMoneyServer);
    }

    /**
     * Starts the main ACS server that handles payment initiation requests.
     * This server is responsible for generating OTP codes and sending them to clients.
     */
    private void startMainServer() {
        log.info("Attempting to start ACS main server on port {}", acsPort);
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsPort,
                acsKeystorePath,
                acsKeystorePassword)) {

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

    /**
     * Starts the ACS Money server that handles OTP verification requests from the Acquirer.
     * This server verifies received tokens against stored AuthToken entries.
     */
    private void startMoneyServer() {
        log.info("Attempting to start ACS Money server on port {}", acsMoneyPort);
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsMoneyPort,
                acsKeystorePath,
                acsKeystorePassword)) {

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
     * Handles client payment initiation requests by:
     * 1. Validating the digital signature of the request
     * 2. Identifying the client based on card details
     * 3. Generating an OTP code and storing it as an AuthToken
     * 4. Sending the OTP to the client via SMS
     * 5. Returning the signed OTP to the caller
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String data = reader.readLine();
            log.debug("ACS Main: Received raw data: {}", data);

            if (data == null || data.isEmpty()) {
                log.warn("ACS Main: No data received from client.");
                writer.println("ERROR:No data received.");
                return;
            }
            Map<String, String> parsedData = DataUtils.parseData(data);
            String source = parsedData.get("source");
            String cardData = parsedData.get("data");
            String signature = parsedData.get("signature");

            if (source == null || cardData == null || signature == null) {
                log.warn("ACS Main: Invalid data format. Missing required fields. Received: {}", data);
                writer.println("ERROR:Invalid data format.");
                return;
            }
            log.info("ACS Main: Processing request from source: {}", source);

            if ("client".equalsIgnoreCase(source)) {
                PublicKey clientPublicKey = KeystoreUtils.getCertificate(
                        clientTruststorePath, clientTruststorePassword, "client_trusted"
                ).getPublicKey();

                if (SignUtils.verifySignature(cardData, signature, clientPublicKey)) {
                    log.info("ACS Main: Client signature verified successfully for data: {}", cardData);
                    Map<String, String> cardDetailsMap = DataUtils.parseData(cardData.replace("#", "&"));
                    String cardNumber = cardDetailsMap.get("card");
                    if (cardNumber == null) {
                        log.warn("ACS Main: Card number missing in data: {}", cardData);
                        writer.println("ERROR:Card number missing.");
                        return;
                    }
                    log.info("ACS Main: Received card number (masked for log): {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));

                    // Find client by card number
                    Optional<Client> clientOpt = clientRepository.findByCards_CardNumber(cardNumber);

                    // If client not found by card number, try alternative method
                    if (!clientOpt.isPresent()) {
                        log.warn("ACS Main: No client found for card number: {}. Trying alternative method.", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));

                        // Alternative: find a client with a phone number (for demo purposes)
                        clientOpt = clientRepository.findAll().stream()
                                .filter(client -> client.getPhoneNumber() != null && !client.getPhoneNumber().isEmpty())
                                .findFirst();

                        if (clientOpt.isPresent()) {
                            log.warn("ACS Main: Using fallback method to find client. This is not recommended for production!");
                        }
                    }

                    if (clientOpt.isPresent()) {
                        Client client = clientOpt.get();
                        log.info("ACS Main: Found client {} associated with the request", client.getEmail());
                        String otpCode = generateOtpCode();
                        log.info("ACS Main: Generated OTP code: {} for client {}", otpCode, client.getEmail());

                        AuthToken token = new AuthToken(otpCode, client);
                        try {
                            tokenRepository.save(token);
                            log.info("ACS Main: Successfully saved AuthToken with ID {} and token value '{}' for client {}", token.getId(), token.getToken(), client.getEmail());
                        } catch (Exception e) {
                            log.error("ACS Main: !!! FAILED to save AuthToken for client {}: {}", client.getEmail(), e.getMessage(), e);
                            writer.println("ERROR:Failed to process token generation.");
                            return;
                        }

                        try {
                            log.info("ACS Main: Attempting to send OTP '{}' via SMS to {}", otpCode, client.getPhoneNumber());
                            smsService.sendOtp(client.getPhoneNumber(), otpCode);
                            log.info("ACS Main: SMS service call completed for client {}", client.getEmail());
                        } catch (RuntimeException e) {
                            log.error("ACS Main: Failed to send SMS OTP to client {}: {}", client.getEmail(), e.getMessage());
                            writer.println("ERROR:Failed to send OTP.");
                            return;
                        }

                        PrivateKey acsPrivateKey = KeystoreUtils.getPrivateKey(
                                acsKeystorePath, acsKeystorePassword, acsKeyAlias, acsKeyPassword
                        );
                        String signedOtpCode = SignUtils.signData(otpCode, acsPrivateKey);
                        String response = "source=" + SOURCE + "&data=" + otpCode + "&signature=" + signedOtpCode;
                        writer.println(response);
                        log.info("ACS Main: Sent response to client with OTP code and signature.");

                    } else {
                        log.warn("ACS Main: No client found for card: {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));
                        writer.println("ERROR:Client not found for this card.");
                    }
                } else {
                    log.warn("ACS Main: Invalid client signature for data: {}", cardData);
                    writer.println("ERROR:Invalid signature.");
                }
            } else {
                log.warn("ACS Main: Invalid source specified: {}. Expected 'client'.", source);
                writer.println("ERROR:Invalid source.");
            }
        } catch (Exception e) {
            log.error("ACS Main: Error handling client request: {}", e.getMessage(), e);
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("ERROR:Internal server error.");
            } catch (Exception ex) {
                log.error("ACS Main: Error sending error response to client: {}", ex.getMessage());
            }
        } finally {
            try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (Exception e) { log.error("ACS Main: Error closing client socket: {}", e.getMessage()); }
        }
    }

    /**
     * Handles OTP verification requests from the Acquirer (ACQ) server.
     * Validates the received OTP token against the stored AuthToken records
     * and returns "ACK" for valid tokens or "NACK" for invalid ones.
     */
    @Transactional
    protected void handleVerificationRequest(SSLSocket socket) {
        String receivedData = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            receivedData = reader.readLine();
            log.info("ACS Money: Received raw data from ACQ: '{}'", receivedData);

            String tokenValue = receivedData;

            if (tokenValue == null || tokenValue.isEmpty() || tokenValue.length() != 6 || !tokenValue.matches("\\d{6}")) {
                log.warn("ACS Money: Invalid or empty token received: '{}'. Sending NACK.", tokenValue);
                writer.println("NACK");
                return;
            }

            log.info("ACS Money: Attempting to verify token '{}'.", tokenValue);
            log.info("ACS Money: Searching for token '{}' in the database.", tokenValue);
            Optional<AuthToken> tokenOpt = tokenRepository.findByToken(tokenValue);

            log.info("ACS Money: Token lookup result for '{}': Found={}", tokenValue, tokenOpt.isPresent());

            if (tokenOpt.isPresent()) {
                AuthToken authToken = tokenOpt.get();
                log.info("ACS Money: Token ID {} found. Checking validity: isUsed={}, expiresAt={}, Now={}",
                        authToken.getId(), authToken.isUsed(), authToken.getExpiresAt(), LocalDateTime.now());

                boolean isValid = authToken.isValid();
                log.info("ACS Money: Result of authToken.isValid() for token ID {}: {}", authToken.getId(), isValid);

                if (isValid) {
                    // Token is valid - mark as used
                    authToken.setUsed(true);
                    tokenRepository.save(authToken);
                    log.info("ACS Money: Token ID {} marked as used. Sending ACK.", authToken.getId());
                    writer.println("ACK");

                } else {
                    // Token is found but invalid (already used or expired)
                    log.warn("ACS Money: Token ID {} found but is invalid (Used={} or Expired={}). Sending NACK.",
                            authToken.getId(), authToken.isUsed(), LocalDateTime.now().isAfter(authToken.getExpiresAt()));

                    writer.println("NACK");
                }

            } else {
                // Token not found in database
                log.warn("ACS Money: Token '{}' not found in database. Sending NACK.", tokenValue);
                writer.println("NACK");
            }

        } catch (Exception e) {
            log.error("ACS Money: Error handling verification request for data '{}': {}", receivedData, e.getMessage(), e);
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println("NACK");
            } catch (Exception ex) {
                log.error("ACS Money: Error sending NACK response after exception: {}", ex.getMessage());
            }
        } finally {
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception e) { log.error("ACS Money: Error closing ACQ socket: {}", e.getMessage()); }
        }
    }

    /**
     * Generates a random 6-digit OTP (One-Time Password) code.
     *
     * @return A 6-digit numeric string
     */
    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}