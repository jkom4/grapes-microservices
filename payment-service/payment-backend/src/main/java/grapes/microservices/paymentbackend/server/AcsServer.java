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
import org.springframework.transaction.annotation.Transactional; // Import pour @Transactional

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class AcsServer implements CommandLineRunner {

    @Value("${app.ports.acs}")
    private int acsPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    // Keystore pour le serveur ACS lui-même
    @Value("${app.keystore.path}") // Probablement app.keystore.acs.path selon application.properties
    private String acsKeystorePath;

    @Value("${app.keystore.password}") // Probablement app.keystore.acs.password
    private String acsKeystorePassword;

    // Clé privée du serveur ACS
    @Value("${app.keystore.key.alias}") // Probablement app.keystore.acs.key.alias
    private String acsKeyAlias;

    @Value("${app.keystore.key.password}") // Probablement app.keystore.acs.key.password
    private String acsKeyPassword;

    // Truststore pour faire confiance au client (payment-backend)
    @Value("${app.truststore.client.path}")
    private String clientTruststorePath;

    @Value("${app.truststore.client.password}")
    private String clientTruststorePassword;

    private final UserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final SmsService smsService;

    private static final String SOURCE = "acs";

    // Map pour stocker les tentatives échouées (optionnel, mais laissé pour le contexte)
    private final Map<Long, Integer> failedAttempts = new ConcurrentHashMap<>();

    @Autowired
    public AcsServer(UserRepository userRepository, AuthTokenRepository tokenRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.smsService = smsService;
    }

    @Override
    public void run(String... args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::startMainServer); // Pour l'initiation (/initiate)
        executorService.submit(this::startMoneyServer); // Pour la vérification (/complete)
    }

    /**
     * Start the main ACS server that handles client requests (initiation)
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
     * Start the ACS Money server that handles verification requests from ACQ
     */
    private void startMoneyServer() {
        log.info("Attempting to start ACS Money server on port {}", acsMoneyPort);
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acsMoneyPort,
                acsKeystorePath,   // Utilise le même keystore/certificat que le port principal
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
     * Handle a client request (from payment-backend's CardService during /initiate)
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        // Logique pour initier: vérifier signature client, générer OTP, sauvegarder, envoyer SMS, renvoyer réponse signée
        // (Cette partie semble fonctionner correctement d'après les logs précédents, on ne la modifie pas pour l'instant)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String data = reader.readLine();
            log.debug("ACS Main: Received raw data: {}", data);

            if (data == null || data.isEmpty()) {
                log.warn("ACS Main: No data received from client.");
                writer.println("ERROR:No data received."); return;
            }
            Map<String, String> parsedData = DataUtils.parseData(data);
            String source = parsedData.get("source");
            String cardData = parsedData.get("data");
            String signature = parsedData.get("signature");

            if (source == null || cardData == null || signature == null) {
                log.warn("ACS Main: Invalid data format. Missing required fields. Received: {}", data);
                writer.println("ERROR:Invalid data format."); return;
            }
            log.info("ACS Main: Processing request from source: {}", source);

            if ("client".equalsIgnoreCase(source)) {
                PublicKey clientPublicKey = KeystoreUtils.getCertificate(
                        clientTruststorePath, clientTruststorePassword, "client_trusted" // Assurez-vous que l'alias est correct
                ).getPublicKey();

                if (SignUtils.verifySignature(cardData, signature, clientPublicKey)) {
                    log.info("ACS Main: Client signature verified successfully for data: {}", cardData);
                    Map<String, String> cardDetailsMap = DataUtils.parseData(cardData.replace("#", "&"));
                    String cardNumber = cardDetailsMap.get("card");
                    if (cardNumber == null) {
                        log.warn("ACS Main: Card number missing in data: {}", cardData);
                        writer.println("ERROR:Card number missing."); return;
                    }
                    log.info("ACS Main: Received card number (masked for log): {}", "****" + cardNumber.substring(Math.max(0, cardNumber.length()-4)));

                    // **LOGIQUE DEMO/PLACEHOLDER POUR TROUVER L'UTILISATEUR**
                    // A Remplacer par une méthode sécurisée pour identifier l'utilisateur associé à la requête/session
                    Optional<User> userOpt = userRepository.findAll().stream()
                            .filter(user -> user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
                            .findFirst(); // **INSECURE DEMO LOGIC**

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        log.info("ACS Main: Found user {} associated with the request (DEMO LOGIC)", user.getLogin());
                        String otpCode = generateOtpCode();
                        log.info("ACS Main: Generated OTP code: {} for user {}", otpCode, user.getLogin());

                        AuthToken token = new AuthToken(otpCode, user);
                        try {
                            tokenRepository.save(token);
                            log.info("ACS Main: Successfully saved AuthToken with ID {} and token value '{}' for user {}", token.getId(), token.getToken(), user.getLogin());
                        } catch (Exception e) {
                            log.error("ACS Main: !!! FAILED to save AuthToken for user {}: {}", user.getLogin(), e.getMessage(), e);
                            writer.println("ERROR:Failed to process token generation."); return;
                        }

                        try {
                            log.info("ACS Main: Attempting to send OTP '{}' via SMS to {}", otpCode, user.getPhoneNumber());
                            smsService.sendOtp(user.getPhoneNumber(), otpCode);
                            log.info("ACS Main: SMS service call completed for user {}", user.getLogin());
                        } catch (RuntimeException e) {
                            log.error("ACS Main: Failed to send SMS OTP to user {}: {}", user.getLogin(), e.getMessage());
                            writer.println("ERROR:Failed to send OTP."); return;
                        }

                        PrivateKey acsPrivateKey = KeystoreUtils.getPrivateKey(
                                acsKeystorePath, acsKeystorePassword, acsKeyAlias, acsKeyPassword
                        );
                        String signedOtpCode = SignUtils.signData(otpCode, acsPrivateKey); // Signe l'OTP, pas la réponse entière
                        String response = "source=" + SOURCE + "&data=" + otpCode + "&signature=" + signedOtpCode; // Renvoie l'OTP et sa signature
                        writer.println(response);
                        log.info("ACS Main: Sent response to client with OTP code and signature.");

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
            // Envoyer une réponse d'erreur générique
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("ERROR:Internal server error.");
            } catch (Exception ex) {
                log.error("ACS Main: Error sending error response to client: {}", ex.getMessage());
            }
        } finally {
            // Fermer le socket
            try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (Exception e) { log.error("ACS Main: Error closing client socket: {}", e.getMessage()); }
        }
    }


    /**
     * Handle a verification request from ACQ (on the money port).
     * VERSION CORRIGÉE AVEC LOGIQUE SIMPLIFIÉE ET LOGS DÉTAILLÉS.
     */
    @Transactional // Ajouté pour s'assurer que le save après setUsed est bien commit
    protected void handleVerificationRequest(SSLSocket socket) { // Changé en protected pour @Transactional
        String receivedData = null; // Pour log même si le parsing échoue
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            receivedData = reader.readLine(); // Lit la ligne brute envoyée par ACQ
            log.info("ACS Money: Received raw data from ACQ: '{}'", receivedData);

            // Supposition: ACQ envoie JUSTE le token OTP maintenant
            String tokenValue = receivedData; // On considère que la ligne entière est le token

            if (tokenValue == null || tokenValue.isEmpty() || tokenValue.length() != 6 || !tokenValue.matches("\\d{6}")) {
                log.warn("ACS Money: Invalid or empty token received: '{}'. Sending NACK.", tokenValue);
                writer.println("NACK"); // Réponse pour token invalide/vide
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

                boolean isValid = authToken.isValid(); // Vérifie isUsed et expiresAt
                log.info("ACS Money: Result of authToken.isValid() for token ID {}: {}", authToken.getId(), isValid);

                // --- Logique Simplifiée ---
                if (isValid) {
                    // Le token est trouvé et valide !
                    authToken.setUsed(true); // Marquer comme utilisé
                    tokenRepository.save(authToken); // Sauvegarder le changement
                    log.info("ACS Money: Token ID {} marked as used. Sending ACK.", authToken.getId());
                    writer.println("ACK"); // Envoyer ACK

                    // Réinitialiser le compteur d'échecs si besoin (logique optionnelle)
                    // if (authToken.getUser() != null) {
                    //     failedAttempts.remove(authToken.getUser().getId());
                    //     log.info("ACS Money: Cleared failed attempts count for user ID {}", authToken.getUser().getId());
                    // }

                } else {
                    // Le token est trouvé mais invalide (déjà utilisé ou expiré)
                    log.warn("ACS Money: Token ID {} found but is invalid (Used={} or Expired={}). Sending NACK.",
                            authToken.getId(), authToken.isUsed(), LocalDateTime.now().isAfter(authToken.getExpiresAt()));

                    // Incrémenter le compteur d'échecs (logique optionnelle)
                    // if (authToken.getUser() != null) {
                    //     int attempts = failedAttempts.compute(authToken.getUser().getId(), (k, v) -> (v == null) ? 1 : v + 1);
                    //     log.warn("ACS Money: User ID {} has {} failed verification attempts (including this one).", authToken.getUser().getId(), attempts);
                    //     // Ajouter logique de blocage si attempts > X
                    // }
                    writer.println("NACK"); // Envoyer NACK
                }
                // --- Fin Logique Simplifiée ---

                // COMMENTED OUT: Logique de mapping de transaction qui causait problème
                 /*
                 log.warn("ACS Money: No token mapping found for transaction '...'. This logic might be incorrect/unnecessary.");
                 // Supprimer ou corriger cette logique si elle n'est pas pertinente pour la validation OTP elle-même.
                 */

                // COMMENTED OUT: Logique de requête utilisateur non nécessaire pour la validation OTP simple
                 /*
                 Hibernate: select u1_0.id ... from users u1_0 where u1_0.id=?
                 log.warn("ACS Money: User query after token check might be unnecessary here.");
                 */


            } else {
                // Le token n'a pas été trouvé dans la base de données
                log.warn("ACS Money: Token '{}' not found in database. Sending NACK.", tokenValue);
                writer.println("NACK"); // Envoyer NACK
            }

        } catch (Exception e) {
            log.error("ACS Money: Error handling verification request for data '{}': {}", receivedData, e.getMessage(), e);
            // Envoyer NACK en cas d'erreur interne
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println("NACK");
            } catch (Exception ex) {
                log.error("ACS Money: Error sending NACK response after exception: {}", ex.getMessage());
            }
        } finally {
            // Fermer le socket
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception e) { log.error("ACS Money: Error closing ACQ socket: {}", e.getMessage()); }
        }
    }


    /**
     * Generate a random 6-digit auth code (OTP)
     */
    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}