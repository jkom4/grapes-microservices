package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.ChallengeWithTimestamp;
import grapes.microservices.authservice.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChallengeServiceTest {

    private ChallengeService challengeService;

    private final User user = new User();

    @BeforeEach
    public void setUp() {
        String email = "user@example.com";
        user.setEmail(email);
        MockitoAnnotations.openMocks(this);
        challengeService = new ChallengeService();

    }

    @Test
    public void testSaveChallengeForUser() {
        String challenge = "Test Challenge";
        ChallengeWithTimestamp challengeWithTimestamp = challengeService.saveChallengeForUser(user.getEmail(), challenge);
        String savedChallenge = challengeWithTimestamp.getChallenge();
        assertEquals(challenge, savedChallenge);
    }

    @Test
    public void testGetChallengeForUserCacheMiss() {
        String email = "user2@example.com";
        String result = challengeService.getChallengeForUser(email).getChallenge();

        assertNull(result);
    }
}