package grapes.microservices.authservice.services;

import grapes.microservices.authservice.dto.EIDRegisterDTO;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.utils.PKCS11Config;
import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.models.Gender;
import iaik.pkcs.pkcs11.*;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.objects.Data;
import iaik.pkcs.pkcs11.objects.PKCS11Object;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EIDCardService {

    @Autowired
    private PKCS11Config pkcs11Config;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final UserService userService;

    public EIDCardInfo readCard() throws Exception {
        Module module = loadModule();
        Slot slot = getSlot(module);
        Token token = slot.getToken();
        Session session = openSession(token);

        try {
            session.login(Session.UserType.USER, null); // null -> middleware manages the PIN

            EIDCardInfo info = new EIDCardInfo();
            populateEIDCardInfo(session, info);

            return info;
        } finally {
            closeSession(session);
        }
    }

    public User registerWithEIDCard(EIDRegisterDTO additionalInfo) throws Exception {
        EIDCardInfo cardInfo = readCard();
        User user = userMapper.toEntityFromEID(cardInfo);

        //set additional info
        user.setEmail(additionalInfo.getEmail());
        user.setPhoneNumber(additionalInfo.getPhoneNumber());
        user.setPassword(additionalInfo.getPassword());
        user.setGender(cardInfo.getGender());

        return userService.registerUser(user);
    }

    /**
     * Verifies the signature of a message using the public key extracted from the eID card.
     *
     * @param messageToVerify The message to verify
     * @param signature       The signature to verify
     * @return true if the signature is valid, false otherwise
     */
    public boolean verifySignature(String messageToVerify, String signature) throws Exception {
        PublicKey publicKey = extractPublicKey();
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        verifier.update(messageToVerify.getBytes(StandardCharsets.UTF_8));
        return verifier.verify(signatureBytes);
    }

    /**
     * Encrypts a message using the public key extracted from the eID card.
     * @param messageToEncrypt The message to encrypt
     * @return The encrypted message in base64 format
     */
    public String encryptMessage(String messageToEncrypt) throws Exception {
        PublicKey publicKey = extractPublicKey();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToEncrypt.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private Module loadModule() throws Exception {
        try {
            return pkcs11Config.getModule();
        } catch (Exception e) {
            throw new Exception("eID middleware not found. Please download and install the middleware from: https://eid.belgium.be/fr/download/15/license", e);
        }
    }

    private Slot getSlot(Module module) throws Exception {
        Slot[] slots = module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
        if (slots.length == 0) {
            throw new Exception("No card detected.");
        }
        return slots[0];
    }

    private Session openSession(Token token) throws TokenException {
        return token.openSession(
                Token.SessionType.SERIAL_SESSION,
                Token.SessionReadWriteBehavior.RO_SESSION,
                null,
                null
        );
    }

    private void populateEIDCardInfo(Session session, EIDCardInfo info) throws TokenException {
        String[] labels = {"surname", "firstnames", "national_number", "date_of_birth", "nationality", "gender"};
        for (String label : labels) {
            String value = readDataFromCard(session, label);
            assignFieldToInfo(info, label, value);
        }
    }

    private String readDataFromCard(Session session, String label) throws TokenException {
        Data template = new Data();
        template.getLabel().setCharArrayValue(label.toCharArray());

        session.findObjectsInit(template);
        PKCS11Object[] found = session.findObjects(1);
        session.findObjectsFinal();

        if (found.length > 0) {
            Data data = (Data) found[0];
            byte[] raw = data.getValue().getByteArrayValue();
            return new String(raw, StandardCharsets.UTF_8).trim();
        }

        return null;
    }

    private void assignFieldToInfo(EIDCardInfo info, String label, String value) {
        if (value == null) return;

        switch (label) {
            case "surname":
                info.setLastName(value);
                break;
            case "firstnames":
                info.setFirstName(value);
                break;
            case "national_number":
                info.setNationalId(value);
                break;
            case "date_of_birth":
                parseAndSetBirthDate(info, value);
                break;
            case "gender":
                setGender(info, value);
                break;
            default:
                break;
        }
    }

    private void parseAndSetBirthDate(EIDCardInfo info, String value) {
        try {
            value = value.trim().replaceAll(" +", " ");
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            info.setBirthDate(formatter.parse(value));
        } catch (Exception ex) {
            System.err.println("Error while parsing date : " + value);
            info.setBirthDate(null);
        }
    }

    private void setGender(EIDCardInfo info, String gender) {
        switch (gender) {
            case "Male":
                info.setGender(Gender.MALE);
                break;
            case "Female":
                info.setGender(Gender.FEMALE);
                break;
            default:
                info.setGender(Gender.OTHER);
                break;
        }
    }

    private void closeSession(Session session) throws TokenException {
        if (session != null) {
            try {
                session.logout();
            } catch (Exception ignore) {
            }
            session.closeSession();
        }
    }

    /**
     * Extracts the public signature key from the eID card.
     */
    private PublicKey extractPublicKey() throws Exception {
        Module module = loadModule();
        Slot slot = getSlot(module);
        Token token = slot.getToken();
        Session session = openSession(token);

        try {
            session.login(Session.UserType.USER, null); // null -> middleware gère le PIN

            X509PublicKeyCertificate cert = getCertificateFromCard(session);
            byte[] certData = cert.getValue().getByteArrayValue();

            return extractPublicKeyFromCertificate(certData);
        } finally {
            closeSession(session);
        }
    }

    private X509PublicKeyCertificate getCertificateFromCard(Session session) throws Exception {
        X509PublicKeyCertificate template = new X509PublicKeyCertificate();
        template.getLabel().setCharArrayValue("Signature".toCharArray());

        session.findObjectsInit(template);
        PKCS11Object[] found = session.findObjects(1);
        session.findObjectsFinal();

        if (found.length == 0) {
            throw new Exception("Signature certificate not found on the card.");
        }

        return (X509PublicKeyCertificate) found[0];
    }

    private PublicKey extractPublicKeyFromCertificate(byte[] certData) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        java.security.cert.Certificate x509 = cf.generateCertificate(new ByteArrayInputStream(certData));
        return x509.getPublicKey();
    }
}