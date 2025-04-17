package grapes.microservices.frontendchat;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.util.Properties;

/**
 * Configuration class for the application.
 * Loads settings from a .env file at the root of the project.
 * Provides static accessors for configuration values.
 */
public final class AppConfig { // final: Prevents subclassing
    private static final Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
                .directory("../") // Adjust path relative to the working directory
                .load();
    }

    public static String get(String property) {
        return dotenv.get(property);
    }
}