package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.User;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

/**
 * Service for interacting with the Belgian eID card.
 * This service handles the reading of the eID card,
 * extraction of user information,
 * and signing messages.
 */
@Service
public class EidCardService {

    private byte[] currentChallenge;

    /**
     * Retrieves the certificate associated with a given user.
     *
     * @param user the user for whom the certificate is to be retrieved
     * @return the X509Certificate associated with the user
     * @throws Exception if an error occurs while retrieving the certificate
     */
    public X509Certificate getCertificateFromUser(User user) throws Exception {
        File configFile = createPkcs11ConfigFile();
        Provider provider = loadPkcs11Provider(configFile);
        KeyStore keyStore = loadKeyStore(provider);

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (cert != null) {
                // Extract DN (Distinguished Name) from certificat
                String dn = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
                // search for the serial number in the DN
                if (dn.contains("SERIALNUMBER=" + user.getNationalId())) {
                    // Delete the temporary config file after use
                    configFile.delete();
                    return cert;
                }
            }
        }

        // Delete the temporary config file if no certificate was found
        configFile.delete();
        throw new Exception("Certificate not found for user with National ID: " + user.getNationalId());
    }

    /**
     * Reads data from the Belgian eID card.
     *
     * @return User information extracted from the eID card
     * @throws Exception if reading or accessing the eID card fails
     */
    public User readCard() throws Exception {
        File configFile = createPkcs11ConfigFile();
        Provider provider = loadPkcs11Provider(configFile);
        KeyStore keyStore = loadKeyStore(provider);

        byte[] challenge = generateChallenge();
        this.currentChallenge = challenge; // Store the challenge
        byte[] message = buildMessage(challenge, "1234");

        User user = new User();

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (cert != null) {
                extractUserFromCertificate(cert, user);
                signMessageIfKeyAvailable(keyStore, alias, message, provider);
                break; // Exit after first valid cert (simplification)
            }
        }

        configFile.delete();
        return user;
    }

    /** Creates a temporary PKCS#11 configuration file. */
    private File createPkcs11ConfigFile() throws Exception {
        String config = """
                name = eID
                library = C:\\Windows\\System32\\beidpkcs11.dll
                slotListIndex = 0
                """;
        File configFile = File.createTempFile("pkcs11", ".cfg");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(config);
        }
        return configFile;
    }

    /** Loads the SunPKCS11 provider using the given config file. */
    private Provider loadPkcs11Provider(File configFile) {
        Provider base = Security.getProvider("SunPKCS11");
        if (base == null) {
            throw new IllegalStateException("SunPKCS11 provider not found.");
        }
        return base.configure(configFile.getAbsolutePath());
    }

    /** Loads the PKCS11 KeyStore. */
    private KeyStore loadKeyStore(Provider provider) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS11", provider);
        keyStore.load(null, null); // PIN entry handled by middleware
        return keyStore;
    }

    /** Generates a random challenge (16 bytes). */
    private byte[] generateChallenge() {
        byte[] challenge = new byte[16];
        new Random().nextBytes(challenge);
        return challenge;
    }

    /** Builds the message to sign using the challenge and a simulated PIN. */
    private byte[] buildMessage(byte[] challenge, String pin) {
        byte[] pinBytes = pin.getBytes(StandardCharsets.UTF_8);
        byte[] message = new byte[challenge.length + pinBytes.length];
        System.arraycopy(challenge, 0, message, 0, challenge.length);
        System.arraycopy(pinBytes, 0, message, challenge.length, pinBytes.length);
        return message;
    }

    /** Extracts first name and surname from the X.500 DN and fills the User object. */
    private void extractUserFromCertificate(X509Certificate cert, User user) {
        String dn = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
        if (dn.contains("GIVENNAME=") && dn.contains("SURNAME=")) {
            user.setFirstName(dn.split("GIVENNAME=")[1].split(",")[0]);
            user.setName(dn.split("SURNAME=")[1].split(",")[0]);
        }
        user.setNationalId("01234567890"); // Example only
        user.setBirthDate(new Date());     // Placeholder
    }

    /** Optionally signs a message using the private key for testing/verification. */
    private void signMessageIfKeyAvailable(KeyStore keyStore, String alias, byte[] message, Provider provider) {
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
            if (privateKey != null) {
                Signature signature = Signature.getInstance("SHA384withECDSA", provider);
                signature.initSign(privateKey);
                signature.update(message);
                byte[] signatureBytes = signature.sign();
                System.out.println("Signature (hex): " + bytesToHex(signatureBytes));
            }
        } catch (Exception e) {
            System.err.println("Error signing message: " + e.getMessage());
        }
    }

    /** Converts a byte array to a hexadecimal string. */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    /** Getter for the current challenge. */
    public byte[] getChallenge() {
        return this.currentChallenge;
    }
}
