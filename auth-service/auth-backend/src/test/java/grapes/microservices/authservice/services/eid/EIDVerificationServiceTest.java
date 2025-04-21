
package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.ChallengeService;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EIDVerificationServiceTest {

    private EIDVerificationService eidVerificationService;
    private ChallengeService challengeService;
    private EIDSignatureService eidSignatureService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        challengeService = mock(ChallengeService.class);
        eidSignatureService = mock(EIDSignatureService.class);
        userService = mock(UserService.class);

        eidVerificationService = new EIDVerificationService();
        eidVerificationService.challengeService = challengeService;
        eidVerificationService.eidSignatureService = eidSignatureService;
        eidVerificationService.userService = userService;
    }

    @Test
    public void testVerifyEIDChallenge_shouldThrow_whenChallengeExpired() {
        String email = "test@expired.com";
        ChallengeWithTimestamp expired = new ChallengeWithTimestamp("xyz");
        expired.setTimestamp(System.currentTimeMillis() - 999999);

        when(challengeService.getChallengeForUser(email)).thenReturn(expired);

        assertThrows(IllegalArgumentException.class, () -> eidVerificationService.verifyEIDChallenge(email, "sig", "1234"));
    }
}
