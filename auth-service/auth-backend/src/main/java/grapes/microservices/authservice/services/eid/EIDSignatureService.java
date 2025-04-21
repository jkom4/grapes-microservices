package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.config.PKCS11Config;
import iaik.pkcs.pkcs11.*;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.objects.PKCS11Object;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.util.Base64;

@Service
public class EIDSignatureService {

    @Autowired
    private PKCS11Config pkcs11Config;

    /**
     * Extracts the public signature key from the eID card.
     */
    public PublicKey extractPublicKey() throws Exception {
        Module module = pkcs11Config.getModule();
        Slot[] slots = module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
        if (slots.length == 0) throw new Exception("No card detected.");

        Token token = slots[0].getToken();
        Session session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);

        try {
            session.login(Session.UserType.USER, null);

            X509PublicKeyCertificate template = new X509PublicKeyCertificate();
            template.getLabel().setCharArrayValue("Signature".toCharArray());

            session.findObjectsInit(template);
            PKCS11Object[] found = session.findObjects(1);
            session.findObjectsFinal();

            if (found.length == 0) {
                throw new Exception("Signature certificate not found on the card.");
            }

            //  Correct cast to X509PublicKeyCertificate
            X509PublicKeyCertificate cert = (X509PublicKeyCertificate) found[0];
            byte[] certData = cert.getValue().getByteArrayValue();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            java.security.cert.Certificate x509 = cf.generateCertificate(new ByteArrayInputStream(certData));
            return x509.getPublicKey();

        } finally {
            session.logout();
            session.closeSession();
        }
    }

    /**
     * Verifies an ECDSA base64-encoded signature for a given message.
     */
    public boolean verifySignature(String base64Signature, String message, PublicKey publicKey) throws Exception {
        byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        Signature verifier = Signature.getInstance("SHA384withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(messageBytes);

        return verifier.verify(signatureBytes);
    }
}