package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.config.PKCS11Config;
import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.models.Gender;
import iaik.pkcs.pkcs11.*;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.objects.Data;
import iaik.pkcs.pkcs11.objects.PKCS11Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Service
public class EIDCardServiceImpl implements EIDCardService {

    @Autowired
    PKCS11Config pkcs11Config;

    @Override
    public EIDCardInfo readCard() throws Exception {
        Module module;
        try {
            module = pkcs11Config.getModule();
        } catch (Exception e) {
            throw new Exception("eID middleware not found. Please download and install the middleware from: https://eid.belgium.be/fr/download/15/license", e);
        }

        Slot[] slots = module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
        if (slots.length == 0) throw new Exception("Aucune carte détectée.");

        Token token = slots[0].getToken();
        Session session = null;

        try {
            session = token.openSession(
                    Token.SessionType.SERIAL_SESSION,
                    Token.SessionReadWriteBehavior.RO_SESSION,
                    null,
                    null
            );

            session.login(Session.UserType.USER, null); // null -> middleware gère le PIN

            EIDCardInfo info = new EIDCardInfo();
            String[] labels = {"surname", "firstnames", "national_number", "date_of_birth", "nationality", "gender"};

            for (String label : labels) {
                Data template = new Data();
                template.getLabel().setCharArrayValue(label.toCharArray());

                session.findObjectsInit(template);
                PKCS11Object[] found = session.findObjects(1);
                session.findObjectsFinal();

                if (found.length > 0) {
                    Data data = (Data) found[0];
                    byte[] raw = data.getValue().getByteArrayValue();
                    String value = new String(raw, StandardCharsets.UTF_8).trim();

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
                            try {
                                value = value.trim().replaceAll(" +", " ");
                                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                                info.setBirthDate(formatter.parse(value));
                            } catch (Exception ex) {
                                System.err.println("Erreur parsing date : " + value);
                                info.setBirthDate(null);
                            }
                            break;
                        case "gender":
                            switch (value.toUpperCase()) {
                                case "M":
                                    info.setGender(Gender.MALE);
                                    break;
                                case "F":
                                    info.setGender(Gender.FEMALE);
                                    break;
                                default:
                                    info.setGender(Gender.OTHER);
                                    break;
                            }
                            break;
                    }
                }
            }

            return info;

        } finally {
            if (session != null) {
                try {
                    session.logout();
                } catch (Exception ignore) {}
                session.closeSession();
            }
        }
    }
    public void setPkcs11Config(PKCS11Config pkcs11Config) {
        this.pkcs11Config = pkcs11Config;
    }
}
