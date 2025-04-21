
package grapes.microservices.authservice.services.eid;

import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EIDSignatureServiceTest {

    @Test
    public void testVerifySignature_shouldReturnFalseForInvalidSignature() throws Exception {
        EIDSignatureService service = new EIDSignatureService();
        PublicKey publicKey = mock(PublicKey.class);
        String base64Signature = Base64.getEncoder().encodeToString("invalid".getBytes());
        assertThrows(Exception.class, () -> {
            service.verifySignature(base64Signature, "message", publicKey);
        });
    }
}
