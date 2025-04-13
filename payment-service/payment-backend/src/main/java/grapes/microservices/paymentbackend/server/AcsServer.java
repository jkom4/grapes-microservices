package grapes.microservices.paymentbackend.server;

import grapes.microservices.paymentbackend.models.AuthToken;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.AuthTokenRepository;
import grapes.microservices.paymentbackend.repositories.UserRepository;
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
import java.util.concurrent.ExecutorService; // Import nécessaire

@Component
@Slf4j
public class AcsServer implements CommandLineRunner {

    @Value("${app.ports.acs}")
    private int acsPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    // Keystore pour le serveur ACS lui-même (utilisé pour créer SSLServerSocket)
    @Value("${app.keystore.path}")
    private String acsKeystorePath;

    @Value("${app.keystore.password}")
    private String acsKeystorePassword;

    // Clé privée du serveur ACS (utilisée pour signer les réponses)
    @Value("${app.keystore.key.alias}")
    private String acsKeyAlias; // Renommé pour clarté

    @Value("${app.keystore.key.password}")
    private String acsKeyPassword; // Renommé pour clarté

    // Truststore pour faire confiance au client (le backend principal)
    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;

    @Value("${app.truststore.client.password}") // Assurez-vous que cette propriété existe dans application.properties
    private String clientTruststorePassword;

    private final UserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    private static final String SOURCE = "acs";

    // Map to store temporary tokens during verification (inchangé)
    private final Map<String, String> pendingTokens = new ConcurrentHashMap<>();

    @Autowired
    public AcsServer(UserRepository userRepository, AuthTokenRepository tokenRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
    }

    @Override
    public void run(String... args) {
        // Use a cached thread pool for better management
        ExecutorService executorService = Executors.newCachedThreadPool();

        // Start the main ACS server in a separate thread
        executorService.submit(this::startMainServer);

        // Start the ACS Money port server in a separate thread
        executorService.submit(this::startMoneyServer);

        // Consider shutting down the executor service gracefully on application exit
        // Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    /**
     * Start the main ACS server that handles client requests
     */
    private void startMainServer() {
        log.info("Attempting to start ACS server on port {}", acsPort);
        // **CORRECTED CALL:** Pass only 3 arguments (port, keystore path, keystore password)
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsPort,
                acsKeystorePath, // Keystore du serveur ACS
                acsKeystorePassword)) { // Mot de passe du keystore ACS

            log.info("ACS server successfully listening on port {}", acsPort);
            // Accept client connections in a loop
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    // Configure client authentication requirement if needed
                    // clientSocket.setNeedClientAuth(true); // Example: Require client cert
                    log.info("ACS main server accepted connection from {}", clientSocket.getRemoteSocketAddress());
                    // Handle each client request in a new thread or using an ExecutorService
                    Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket));
                } catch (Exception e) {
                    log.error("Error accepting connection on ACS main port {}: {}", acsPort, e.getMessage(), e);
                    // Optionally break the loop or wait before retrying
                }
            }
        } catch (Exception e) {
            log.error("Fatal error starting ACS main server on port {}: {}", acsPort, e.getMessage(), e);
            // Consider application shutdown or alternative actions
        }
    }

    /**
     * Start the ACS Money server that handles verification requests
     */
    private void startMoneyServer() {
        log.info("Attempting to start ACS Money server on port {}", acsMoneyPort);
        // **CORRECTED CALL:** Pass only 3 arguments (port, keystore path, keystore password)
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsMoneyPort,
                acsKeystorePath,       // Assuming same keystore for simplicity
                acsKeystorePassword)) {

            log.info("ACS Money server successfully listening on port {}", acsMoneyPort);
            // Accept connections in a loop
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SSLSocket acqSocket = (SSLSocket) serverSocket.accept();
                    log.info("ACS Money server accepted connection from {}", acqSocket.getRemoteSocketAddress());
                    // Handle each verification request
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
     * Handle a client request (from payment-backend's CardService)
     * @param clientSocket the client socket
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String data = reader.readLine();
            log.debug("ACS Main: Received raw data: {}", data); // Debug level for potentially sensitive data

            if (data == null || data.isEmpty()) {
                log.warn("ACS Main: No data received from client.");
                writer.println("ERROR:No data received."); // Provide clearer error
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

            // Expecting requests from the "client" (payment-backend)
            if ("client".equalsIgnoreCase(source)) {
                // Verify signature using client's public key from the truststore
                PublicKey clientPublicKey = KeystoreUtils.getCertificate(
                        clientTruststorePath, // Path to truststore containing the client's cert
                        clientTruststorePassword, // Password for this truststore
                        "client_trusted" // Alias of the client's certificate in the truststore
                ).getPublicKey();

                if (SignUtils.verifySignature(cardData, signature, clientPublicKey)) {
                    log.info("ACS Main: Client signature verified successfully for data: {}", cardData);

                    // Parse card data (format: "card=XXXX#date=MM/YYYY")
                    // Consider a more robust parsing method (e.g., regex or dedicated parser)
                    Map<String, String> cardDetailsMap = DataUtils.parseData(cardData.replace("#", "&")); // Reuse parseData
                    String cardNumber = cardDetailsMap.get("card");
                    String expirationDate = cardDetailsMap.get("date"); // Not used here, but parsed
                    // String amount = cardDetailsMap.get("amount"); // If amount/merchant were included
                    // String merchant = cardDetailsMap.get("merchant");

                    if (cardNumber == null) {
                        log.warn("ACS Main: Card number missing in data: {}", cardData);
                        writer.println("ERROR:Card number missing.");
                        return;
                    }

                    log.info("ACS Main: Received card number: {}", cardNumber); // Masking recommended for logs

                    // Find user associated with the card - **CRITICAL**: This is highly insecure and needs replacement
                    // In a real system, the user would be identified securely, perhaps via session or token from payment backend.
                    // This placeholder finds ANY user with a phone number.
                    Optional<User> userOpt = userRepository.findAll().stream()
                            .filter(user -> user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
                            .findFirst(); // **INSECURE DEMO LOGIC**

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        log.info("ACS Main: Found user {} associated with the request (DEMO LOGIC)", user.getLogin());

                        // Generate OTP code
                        String otpCode = generateOtpCode();
                        log.info("ACS Main: Generated OTP code: {} for user {}", otpCode, user.getLogin());

                        // Store auth code linked to the user - consider expiration
                        AuthToken token = new AuthToken(otpCode, user);
                        tokenRepository.save(token);
                        log.info("ACS Main: Saved OTP token {} for user {}", token.getId(), user.getLogin());

                        // Send OTP via SMS (using configured SmsService)
                        try {
                            smsService.sendOtp(user.getPhoneNumber(), otpCode);
                            log.info("ACS Main: OTP sent via SMS to user {}", user.getLogin());
                        } catch (RuntimeException e) {
                            log.error("ACS Main: Failed to send SMS OTP to user {}: {}", user.getLogin(), e.getMessage());
                            writer.println("ERROR:Failed to send OTP.");
                            // Decide if the process should fail here or continue without SMS confirmation
                            // For 3DS, failing here is usually correct.
                            return;
                        }

                        // Sign the OTP code with ACS's private key
                        PrivateKey acsPrivateKey = KeystoreUtils.getPrivateKey(
                                acsKeystorePath,
                                acsKeystorePassword,
                                acsKeyAlias, // Use ACS key alias
                                acsKeyPassword // Use ACS key password
                        );

                        String signedOtpCode = SignUtils.signData(otpCode, acsPrivateKey);

                        // Return the signed OTP code
                        String response = "source=" + SOURCE + "&data=" + otpCode + "&signature=" + signedOtpCode;
                        writer.println(response);
                        log.info("ACS Main: Sent response to client with OTP code.");

                    } else {
                        log.warn("ACS Main: No user found for verification (using demo logic). Card: {}", cardNumber);
                        writer.println("ERROR:User not found for this card.");
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
            // Avoid sending detailed errors back to the client
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("ERROR:Internal server error.");
            } catch (Exception ex) {
                log.error("ACS Main: Error sending error response to client: {}", ex.getMessage());
            }
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    log.debug("ACS Main: Closed client socket.");
                }
            } catch (Exception e) {
                log.error("ACS Main: Error closing client socket: {}", e.getMessage());
            }
        }
    }


    /**
     * Handle a verification request from ACQ (on the money port)
     * @param socket the client socket (ACQ)
     */
    private void handleVerificationRequest(SSLSocket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String tokenValue = reader.readLine(); // Renamed for clarity
            log.debug("ACS Money: Received raw verification request: {}", tokenValue);

            if (tokenValue == null || tokenValue.isEmpty()) {
                writer.println("NACK"); // Negative Acknowledgement
                log.warn("ACS Money: Empty token received for verification.");
                return;
            }

            // Check if token exists in database and is valid
            // Use findByToken as the ACQ doesn't know the user
            Optional<AuthToken> tokenOpt = tokenRepository.findByToken(tokenValue);

            if (tokenOpt.isPresent()) {
                AuthToken authToken = tokenOpt.get();
                User tokenUser = authToken.getUser(); // Get associated user for logging
                log.info("ACS Money: Found token {} associated with user {}", tokenValue, tokenUser != null ? tokenUser.getLogin() : "UNKNOWN");

                // Check if token is valid (not expired and not used)
                if (authToken.isValid()) { // Use the isValid method from AuthToken model
                    // Mark token as used to prevent replay attacks
                    authToken.setUsed(true);
                    tokenRepository.save(authToken);

                    writer.println("ACK"); // Positive Acknowledgement
                    log.info("ACS Money: Token verified successfully and marked as used: {}", tokenValue);
                } else {
                    writer.println("NACK");
                    log.warn("ACS Money: Token expired or already used: {}. Expired: {}, Used: {}",
                            tokenValue,
                            LocalDateTime.now().isAfter(authToken.getExpiresAt()),
                            authToken.isUsed());
                }
            } else {
                writer.println("NACK");
                log.warn("ACS Money: Token not found in database: {}", tokenValue);
            }
        } catch (Exception e) {
            log.error("ACS Money: Error handling verification request: {}", e.getMessage(), e);
            // Send NACK if possible, otherwise the connection might just close
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println("NACK");
            } catch (Exception ex) {
                log.error("ACS Money: Error sending NACK response: {}", ex.getMessage());
            }
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    log.debug("ACS Money: Closed ACQ socket.");
                }
            } catch (Exception e) {
                log.error("ACS Money: Error closing ACQ socket: {}", e.getMessage());
            }
        }
    }


    /**
     * Generate a random 6-digit auth code (OTP)
     * @return the OTP code
     */
    private String generateOtpCode() { // Renamed for clarity
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // Ensure 6 digits with leading zeros
    }
}