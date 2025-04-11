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
    public static SSLSocket createSslClientSocket(int port, String truststorePath) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ts = KeyStore.getInstance("JKS");

        // load the TrustStore containing the trusted certificates
        ts.load(new FileInputStream(truststorePath), "komnoupoueamine".toCharArray());
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
     * @param truststorePath The path to the truststore file
     * @return A configured SSLServerSocket instance.
     */
    public static SSLServerSocket createSslServerSocket(int port, String truststorePath) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");

        ks.load(new FileInputStream(truststorePath), "komnoupoueamine".toCharArray());
        kmf.init(ks, "komnoupoueamine".toCharArray());

        sslContext.init(kmf.getKeyManagers(), null, null);
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        return (SSLServerSocket) ssf.createServerSocket(port);
    }
}