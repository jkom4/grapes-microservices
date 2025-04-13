package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.models.Gender;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.objects.Data;
import iaik.pkcs.pkcs11.objects.PKCS11Object;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;

@Service
public class EIDCardServiceImpl implements EIDCardService {

    @Override
    public EIDCardInfo readCard() throws Exception {
        Module module = Module.getInstance("C:\\Windows\\System32\\beidpkcs11.dll");
        module.initialize(null);

        Slot[] slots = module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
        if (slots.length == 0) throw new Exception("Aucune carte détectée.");

        Token token = slots[0].getToken();
        Session session = token.openSession(
                Token.SessionType.SERIAL_SESSION,
                Token.SessionReadWriteBehavior.RO_SESSION,
                null,
                null
        );


        session.login(Session.UserType.USER, null);

        EIDCardInfo info = new EIDCardInfo();
        String[] labels = {"surname", "firstnames", "national_number", "date_of_birth", "nationality","gender"};
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
                    case "surname": info.setLastName(value); break;
                    case "firstnames": info.setFirstName(value); break;
                    case "national_number": info.setNationalId(value); break;
                    case "date_of_birth":
                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
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
        session.logout();
        session.closeSession();
        module.finalize(null);
        return info;
    }
}