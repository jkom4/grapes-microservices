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

    @Value("${app.ports.acq}")
    private int acqPort;

    @Value("${app.ports.acs.money}")
    private int acsMoneyPort;



    @Value("${app.keystore.acq.path}")
    private String acqKeystorePath;

    @Value("${app.keystore.acq.password}") // Password for the ACQ keystore
    private String acqKeystorePassword;

    @Value("${app.keystore.acq.key.password}") // Password for the key within ACQ keystore
    private String acqKeyPassword;

    @Value("${app.truststore.acs.path}")
    private String acsTruststorePath;

    @Value("${app.keystore.password}") // Assuming ACS truststore uses this password
    private String acsTruststorePassword;

    private static final String SOURCE = "acq";

    @Override
    public void run(String... args) {
        // Start the ACQ server in a separate thread
        Executors.newSingleThreadExecutor().submit(this::startServer);
    }

    /**
     * Start the ACQ server
     */
    private void startServer() {
        log.info("Starting ACQ server on port {}", acqPort);

        // CORRECTED CALL: Pass only 3 arguments
        try (SSLServerSocket serverSocket = SslUtils.createSslServerSocket(
                acqPort,
                acqKeystorePath,        // Keystore du serveur ACQ
                acqKeystorePassword)) { // Mot de passe du keystore ACQ
            log.info("ACQ server listening on port {}", acqPort);
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                log.info("Client connected to ACQ server");

                // Handle client connection in a separate thread
                Executors.newSingleThreadExecutor().submit(() -> handleClientRequest(clientSocket));
            }
        } catch (Exception e) {
            log.error("Error in ACQ server: {}", e.getMessage(), e);
        }
    }




    /**
     * Handle a client request
     * @param clientSocket the client socket
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String token = reader.readLine();
            log.info("Received token from client: {}", token);

            if (token == null || token.isEmpty()) {
                writer.println("Response from ACQ: Invalid token");
                return;
            }

            // Send the token to ACS for verification
            String responseFromAcs = sendTokenToAcs(token);
            log.info("Response from ACS: {}", responseFromAcs);

            // Forward the response back to the client
            writer.println("Response from ACQ: " + responseFromAcs);
        } catch (Exception e) {
            log.error("Error handling client request: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a token to ACS for verification
     * @param token the token to verify
     * @return the response from ACS (ACK or NACK)
     */
    private String sendTokenToAcs(String token) {
        log.info("Sending token to ACS for verification: {}", token);

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsMoneyPort,
                acsTruststorePath,
                acsTruststorePassword // Pass the truststore password
        ))  {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            // Send the token to ACS
            writer.println(token);
            log.info("Token sent to ACS");

            // Receive the response from ACS
            String response = reader.readLine();
            log.info("Received response from ACS: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACS: {}", e.getMessage(), e);
            return "NACK";
        }
    }
}