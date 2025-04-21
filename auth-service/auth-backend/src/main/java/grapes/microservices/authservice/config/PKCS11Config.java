package grapes.microservices.authservice.config;

import iaik.pkcs.pkcs11.Module;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Configuration class for PKCS#11 module initialization.
 * Handles the setup of the Belgian eID card reader middleware.
 */
@Component
@Profile("!test") // Exclude this bean during tests
public class PKCS11Config {

    private Module module;

    /**
     * Initializes the PKCS#11 module for Belgian eID card access.
     * Loads the Windows DLL module and initializes it.
     * @throws RuntimeException if module initialization fails
     */
    @PostConstruct
    public void init() {
        try {
            module = Module.getInstance("C:\\Windows\\System32\\beidpkcs11.dll");
            module.initialize(null);
        } catch (Exception e) {
            throw new RuntimeException("PKCS#11 module initialization error: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the initialized PKCS#11 module instance.
     * @return the PKCS#11 module
     */
    public Module getModule() {
        return module;
    }
}