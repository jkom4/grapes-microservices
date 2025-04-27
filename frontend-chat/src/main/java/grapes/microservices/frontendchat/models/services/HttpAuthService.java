package grapes.microservices.frontendchat.models.services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import javafx.beans.property.SimpleStringProperty;

public class HttpAuthService implements IHttpAuthService {

    private HttpServer server;
    private final String contextPath;
    private final String authUrl; // url used to redirect user to auth page
    // When server is started, the return url will be created, user can be redirected with the return url
    private SimpleStringProperty redirectUrlObserver = new SimpleStringProperty();
    // Once the temporary server detect a token (authentication successful), it notify the listeners
    private SimpleStringProperty tokenObserver =  new SimpleStringProperty();

    public HttpAuthService(String contextPath, String authUrl) {
        this.contextPath = contextPath;
        this.authUrl = authUrl;
    }

    @Override
    public void setRedirectUrlObserver(SimpleStringProperty redirectUrlObserver) {
        this.redirectUrlObserver = redirectUrlObserver;
    }

    @Override
    public void setTokenObserver(SimpleStringProperty tokenObserver) {
        this.tokenObserver = tokenObserver;
    }

    /**
     * Starts the HTTP server. If it’s already running, this is a no‑op.
     */
    @Override
    public synchronized void start() throws IOException {
        if (server != null) {
            System.out.println("[HttpAuthService] Server is already running on port " + getPort());
            return;
        }

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(contextPath, exchange -> {
            // Display html
            String response = HtmlPages.AUTH_SUCCESS_PAGE;
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            // Extract the query from the request URI
            String query = exchange.getRequestURI().getQuery();
            String accessToken = "";
            if (query != null && query.contains("accessToken=")) {
                accessToken = query.split("accessToken=")[1].split("&")[0];
            }

            // notify auth success
            tokenObserver.set(accessToken);
        });
        server.setExecutor(null); // default executor
        server.start();

        String returnUrl =  "http://localhost:" + getPort() + contextPath + "?accessToken=";
        redirectUrlObserver.set(authUrl + "?" + "redirect=" + returnUrl);
        System.out.println("[HttpAuthService] Server started on port " + getPort() + " (context: " + contextPath + ")");
        System.out.println("[HttpAuthService] Listening at " + returnUrl);
    }

    /**
     * Returns the port the server is listening on, or -1 if not running.
     */
    private synchronized int getPort() {
        return (server != null)
                ? server.getAddress().getPort()
                : -1;
    }

    /**
     * Stops the HTTP server. If it’s not running, this is a no‑op.
     */
    @Override
    public synchronized void stop() {
        if (server == null) {
            System.out.println("[HttpAuthService] Server is not running.");
            return;
        }

        server.stop(1);
        server = null;
        System.out.println("[HttpAuthService] Server stopped.");
    }
}

