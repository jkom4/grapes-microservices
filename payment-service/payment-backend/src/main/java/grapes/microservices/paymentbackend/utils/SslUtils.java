package grapes.microservices.paymentbackend.utils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Utility for SSL socket operations.
 * Provides methods to create secure client and server sockets.
 */
public class SslUtils {

    private static final String HOST = "localhost";

    /**
     * Creates an SSL client socket to connect to a server.
     * Configures the socket with the given truststore for server authentication.
     *
     * @param port The server port to connect to
     * @param truststorePath Path to the truststore containing trusted server certificates
     * @param truststorePassword Password for the truststore
     * @return The configured SSL client socket
     * @throws Exception If socket creation fails
     */
    public static SSLSocket createSslClientSocket(int port, String truststorePath, String truststorePassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ts = KeyStore.getInstance("JKS");

        // Load the TrustStore containing the trusted certificates
        ts.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
        tmf.init(ts);

        sslContext.init(null, tmf.getTrustManagers(), null);

        // Create a secure SSL connection with the server
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        return (SSLSocket) ssf.createSocket(HOST, port);
    }

    /**
     * Creates an SSL server socket for accepting secure client connections.
     * Configures the socket with the given keystore for server authentication.
     *
     * @param port The port to listen on
     * @param keystorePath Path to the keystore containing the server's private key
     * @param keystorePassword Password for the keystore
     * @return The configured SSL server socket
     * @throws Exception If socket creation fails
     */
    public static SSLServerSocket createSslServerSocket(int port, String keystorePath, String keystorePassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");

        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        // Assuming key password is the same as keystore password
        kmf.init(ks, keystorePassword.toCharArray());

        sslContext.init(kmf.getKeyManagers(), null, null);
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        return (SSLServerSocket) ssf.createServerSocket(port);
    }
}