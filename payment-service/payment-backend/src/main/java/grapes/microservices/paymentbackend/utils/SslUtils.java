package grapes.microservices.paymentbackend.utils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SslUtils {

    private static final String HOST = "localhost";

    /**
     * Create a SSL client socket to connect to a server.
     * @param port the server port
     * @return the SSL client socket
     */
    public static SSLSocket createSslClientSocket(int port, String truststorePath, String truststorePassword) throws Exception { // Added truststorePassword parameter
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ts = KeyStore.getInstance("JKS");

        // load the TrustStore containing the trusted certificates
        // Use the provided password parameter
        ts.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
        tmf.init(ts);

        sslContext.init(null, tmf.getTrustManagers(), null);

        // create a secure SSL connection with the server
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        return (SSLSocket) ssf.createSocket(HOST, port);
    }

    /**
     * Creates an SSL server socket configured with the server's key store for secure communication.
     *
     * @param port The port number to listen on

     * @return A configured SSLServerSocket instance.
     */
    public static SSLServerSocket createSslServerSocket(int port, String keystorePath, String keystorePassword) throws Exception { // Added keystorePassword parameter
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");

        // Use the provided password parameter instead of the hardcoded one
        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        // Assuming key password is the same as keystore password, adjust if needed
        kmf.init(ks, keystorePassword.toCharArray());

        sslContext.init(kmf.getKeyManagers(), null, null);
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        return (SSLServerSocket) ssf.createServerSocket(port);
    }
}