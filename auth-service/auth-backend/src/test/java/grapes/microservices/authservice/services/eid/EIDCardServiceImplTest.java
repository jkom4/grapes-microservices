package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.config.PKCS11Config;
import grapes.microservices.authservice.dto.EIDCardInfo;
import iaik.pkcs.pkcs11.*;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.objects.Data;
import iaik.pkcs.pkcs11.objects.PKCS11Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EIDCardServiceImplTest {

    private EIDCardServiceImpl eidCardService;
    private PKCS11Config pkcs11Config;
    private Module module;
    private Slot slot;
    private Token token;
    private Session session;

    @BeforeEach
    public void setup() throws Exception {
        pkcs11Config = mock(PKCS11Config.class);
        module = mock(Module.class);
        slot = mock(Slot.class);
        token = mock(Token.class);
        session = mock(Session.class);

        when(pkcs11Config.getModule()).thenReturn(module);
        when(module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT)).thenReturn(new Slot[]{slot});
        when(slot.getToken()).thenReturn(token);
        when(token.openSession(any(), any(), isNull(), isNull())).thenReturn(session);

        eidCardService = new EIDCardServiceImpl();
        eidCardService.setPkcs11Config(pkcs11Config); // setter pour le test
    }

    @Test
    public void testReadCard_shouldReturnEIDCardInfo() throws Exception {
        mockFindObject("surname", "Doe");
        mockFindObject("firstnames", "John");
        mockFindObject("national_number", "12345678901");
        mockFindObject("gender", "M");
        mockFindObject("date_of_birth", "01 JAN 1990");

        EIDCardInfo info = eidCardService.readCard();

        assertNotNull(info);
        assertEquals("Doe", info.getLastName());
        assertEquals("John", info.getFirstName());
        assertEquals("12345678901", info.getNationalId());
        assertEquals("MALE", info.getGender().name());
        assertNotNull(info.getBirthDate());
    }

    @Test
    public void testReadCard_shouldThrowWhenNoCardPresent() throws Exception {
        when(module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT)).thenReturn(new Slot[]{});

        Exception ex = assertThrows(Exception.class, () -> eidCardService.readCard());
        assertTrue(ex.getMessage().contains("Aucune carte détectée"));
    }

    @Test
    public void testReadCard_shouldHandleDateParseError() throws Exception {
        mockFindObject("date_of_birth", "bad date");

        EIDCardInfo info = eidCardService.readCard();
        assertNull(info.getBirthDate());
    }

    @Test
    public void testReadCard_shouldReturnNullGenderWhenInvalid() throws Exception {
        mockFindObject("gender", "X");
        EIDCardInfo info = eidCardService.readCard();
        assertEquals("OTHER", info.getGender().name());
    }

    @Test
    public void testReadCard_shouldSkipIfFieldNotFound() throws Exception {
        doNothing().when(session).login(Session.UserType.USER, null);
        doNothing().when(session).logout();
        doNothing().when(session).closeSession();

        doNothing().when(session).findObjectsInit(any());
        when(session.findObjects(anyInt())).thenReturn(new PKCS11Object[0]);
        doNothing().when(session).findObjectsFinal();

        EIDCardInfo info = eidCardService.readCard();
        assertNotNull(info);
    }


    private void mockFindObject(String label, String value) throws Exception {
        Data data = new Data();
        data.getLabel().setCharArrayValue(label.toCharArray());
        data.getValue().setByteArrayValue(value.getBytes());

        // Mock l'init de la recherche d'objets
        doNothing().when(session).findObjectsInit(any());

        // Retourne l'objet PKCS11 correspondant au label
        when(session.findObjects(anyInt())).thenReturn(new PKCS11Object[]{data});

        // Mock la fin de la recherche
        doNothing().when(session).findObjectsFinal();

        // ✅ Pas de any() ici, on utilise des valeurs concrètes
        doNothing().when(session).login(Session.UserType.USER, null);
        doNothing().when(session).logout();
        doNothing().when(session).closeSession();
    }

}