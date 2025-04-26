package grapes.microservices.frontendchat;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class for the application.
 * Loads settings from a .env file at the root of the project.
 * Provides static accessors for configuration values.
 */
public enum AppEnv { // final modifier not needed for enums as they're implicitly final
    CHAT_SERVICE_URL("CHAT_SERVICE_URL"),
    CHAT_SERVICE_PORT("CHAT_SERVICE_PORT"),
    PUSHER_CLUSTER("PUSHER_CLUSTER"),
    PUSHER_APP_KEY("PUSHER_APP_KEY"),

    AUTH_SERVICE_URL("AUTH_SERVICE_URL");

    private static final Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
//                .directory("../") // Adjust path relative to the working directory
                .load();
    }

    private final String property;

    AppEnv(String property) {
        this.property = property;
    }

    public String get() {
        return dotenv.get(property);
    }
}
