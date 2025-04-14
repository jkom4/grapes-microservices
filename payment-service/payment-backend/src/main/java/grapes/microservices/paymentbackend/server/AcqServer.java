package grapes.microservices.paymentbackend.server;

import grapes.microservices.paymentbackend.utils.SslUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class AcqServer implements CommandLineRunner {

    // ... (Vos @Value et autres champs restent inchangés) ...
    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;

    @Value("${app.keystore.acq.path}")
    private String acqKeystorePath;

    @Value("${app.keystore.acq.password}")
    private String acqKeystorePassword;

    @Value("${app.keystore.acq.key.password}")
    private String acqKeyPassword;

    @Value("${app.truststore.acs.path}")
    private String acsTruststorePath;

    // Attention: La propriété reference app.keystore.password ici, assurez-vous que c'est le bon mot de passe pour acsTruststore
    @Value("${app.keystore.password}")
    private String acsTruststorePassword;


    @Override
    public void run(String... args) {
        Executors.newSingleThreadExecutor().submit(this::startServer);
    }

    private void startServer() {
        // ... (startServer reste inchangé) ...
        log.info("Starting ACQ server on port {}", acqPort);

        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acqPort,
                acqKeystorePath,
                acqKeystorePassword)) {
            log.info("ACQ server listening on port {}", acqPort);
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                log.info("Client connected to ACQ server");
                Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket));
            }
        } catch (Exception e) {
            log.error("Error in ACQ server: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle a client request - VERSION CORRIGÉE
     * @param clientSocket the client socket
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestData = reader.readLine(); // Reçoit "580856"
            log.info("Received data from client: {}", requestData);

            if (requestData == null || requestData.isEmpty()) {
                writer.println("Response from ACQ: Invalid request data");
                return;
            }

            // On suppose que le client (PaymentService) envoie JUSTE le token maintenant.
            // Donc requestData EST le token.
            String token = requestData.trim(); // Utilise directement requestData comme token

            // Validation simple du format (optionnel mais recommandé)
            if (token.length() != 6 || !token.matches("\\d{6}")) {
                log.warn("ACQ Server: Received data does not look like a 6-digit token: '{}'", token);
                writer.println("Response from ACQ: Invalid token format received");
                return;
            }


            // --- CORRECTION ICI ---
            // Appeler sendTokenToAcs avec SEULEMENT le token
            log.info("Forwarding token to ACS for verification: {}", token);
            String responseFromAcs = sendTokenToAcs(token); // Passe juste le token
            // --- FIN CORRECTION ---

            log.info("Received response from ACS: {}", responseFromAcs); // Log la réponse reçue (ACK ou NACK)

            // Transférer la réponse de l'ACS telle quelle (ou préfixée comme avant)
            writer.println("Response from ACQ: " + responseFromAcs); // Renvoie "Response from ACQ: ACK" ou "Response from ACQ: NACK"

        } catch (Exception e) {
            log.error("Error handling client request: {}", e.getMessage(), e);
            // Essayer d'envoyer une erreur au client si possible
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("Response from ACQ: Error processing request");
            } catch (Exception ex) {
                log.error("Error sending error response from ACQ: {}", ex.getMessage());
            }
        } finally {
            try { if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch(Exception e){log.error("Error closing client socket in ACQ: {}", e.getMessage());}
        }
    }

    /**
     * Send ONLY the OTP token to ACS - SIGNATURE ET CORPS CORRIGÉS
     * @param token The 6-digit OTP token
     * @return the response from ACS (ACK or NACK)
     */
    private String sendTokenToAcs(String token) { // Ne prend que le token en argument
        log.info("Sending token ONLY to ACS Money port: {}", token);

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsMoneyPort,
                acsTruststorePath,
                acsTruststorePassword
        ))  {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            // --- CORRECTION ICI ---
            // Envoyer seulement le token
            writer.println(token);
            log.info("Token sent to ACS: {}", token);
            // --- FIN CORRECTION ---

            // Recevoir la réponse de l'ACS
            String response = reader.readLine();
            log.info("Received response from ACS: {}", response); // Devrait être ACK ou NACK

            return response; // Renvoie ACK ou NACK
        } catch (Exception e) {
            log.error("Error communicating with ACS Money port: {}", e.getMessage(), e);
            return "NACK"; // Retourne NACK en cas d'erreur de communication
        }
    }
}