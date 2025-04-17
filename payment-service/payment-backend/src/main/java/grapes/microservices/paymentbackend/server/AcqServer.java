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

/**
 * Server component that acts as an Acquirer (ACQ) in the payment processing flow.
 * Receives payment verification tokens and forwards them to the Authentication Server (ACS).
 */
@Component
@Slf4j
public class AcqServer implements CommandLineRunner {

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

    @Value("${app.keystore.password}")
    private String acsTruststorePassword;

    /**
     * Starts the ACQ server in a separate thread when application launches.
     */
    @Override
    public void run(String... args) {
        Executors.newSingleThreadExecutor().submit(this::startServer);
    }

    /**
     * Initializes and starts the SSL server socket that listens for client connections.
     * Handles each client connection in a separate thread.
     */
    private void startServer() {
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
     * Processes client requests by receiving a 6-digit OTP token and
     * forwarding it to the ACS for verification.
     *
     * @param clientSocket The SSL socket for the client connection
     */
    private void handleClientRequest(SSLSocket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestData = reader.readLine();
            log.info("Received data from client: {}", requestData);

            if (requestData == null || requestData.isEmpty()) {
                writer.println("Response from ACQ: Invalid request data");
                return;
            }

            String token = requestData.trim();

            // Validate token format
            if (token.length() != 6 || !token.matches("\\d{6}")) {
                log.warn("ACQ Server: Received data does not look like a 6-digit token: '{}'", token);
                writer.println("Response from ACQ: Invalid token format received");
                return;
            }

            // Forward token to ACS for verification
            log.info("Forwarding token to ACS for verification: {}", token);
            String responseFromAcs = sendTokenToAcs(token);

            log.info("Received response from ACS: {}", responseFromAcs);

            // Return the response from ACS to the client
            writer.println("Response from ACQ: " + responseFromAcs);

        } catch (Exception e) {
            log.error("Error handling client request: {}", e.getMessage(), e);
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                writer.println("Response from ACQ: Error processing request");
            } catch (Exception ex) {
                log.error("Error sending error response from ACQ: {}", ex.getMessage());
            }
        } finally {
            try {
                if(clientSocket != null && !clientSocket.isClosed())
                    clientSocket.close();
            } catch(Exception e) {
                log.error("Error closing client socket in ACQ: {}", e.getMessage());
            }
        }
    }

    /**
     * Forwards the received OTP token to the ACS server for validation.
     *
     * @param token The 6-digit OTP token to validate
     * @return "ACK" if the token is valid, "NACK" otherwise
     */
    private String sendTokenToAcs(String token) {
        log.info("Sending token ONLY to ACS Money port: {}", token);

        try (SSLSocket acsSocket = SslUtils.createSslClientSocket(
                acsMoneyPort,
                acsTruststorePath,
                acsTruststorePassword
        ))  {
            PrintWriter writer = new PrintWriter(acsSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acsSocket.getInputStream()));

            writer.println(token);
            log.info("Token sent to ACS: {}", token);

            String response = reader.readLine();
            log.info("Received response from ACS: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Error communicating with ACS Money port: {}", e.getMessage(), e);
            return "NACK";
        }
    }
}