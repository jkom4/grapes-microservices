package grapes.microservices.authservice.utils;

import iaik.pkcs.pkcs11.Module;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Configuration class for PKCS#11 module initialization.
 * Handles the setup of the Belgian eID card reader middleware.
 */
@Getter
@Component
@Profile("!test") // Exclude this bean during tests
public class PKCS11Config {

    /**
     * -- GETTER --
     *  Gets the initialized PKCS#11 module instance.
     *
     */
    private Module module;

    /**
     * Initializes the PKCS#11 module for Belgian eID card access.
     * Loads the Windows DLL module and initializes it.
     * @throws RuntimeException if module initialization fails
     */
    @PostConstruct
    public void init() {
        try {
            String pkcs11LibPath = null;

            String[] possiblePaths = {
                    "/Library/OpenSC/lib/opensc-pkcs11.so",                             // macOS OpenSC
                    "/opt/homebrew/lib/opensc-pkcs11.so",                               // macOS M1/M2
                    "/usr/local/lib/opensc-pkcs11.so",                                  // Linux / custom install
                    "/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so",                       // Debian-based Linux
                    "C:\\Windows\\System32\\beidpkcs11.dll",                            // Windows BE eID
                    "C:\\Program Files\\OpenSC Project\\OpenSC\\pkcs11\\opensc-pkcs11.dll" // Windows OpenSC
            };

            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    pkcs11LibPath = path;
                    break;
                }
            }
            if (pkcs11LibPath == null) {
                throw new RuntimeException("Could not find a valid PKCS#11 module.");
            }

            module = Module.getInstance(pkcs11LibPath);
            module.initialize(null);
        } catch (Exception e) {
            throw new RuntimeException("PKCS#11 module initialization error: " + e.getMessage(), e);
        }
    }
}