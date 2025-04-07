package grapes.microservices.authservice.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmsServiceTest {

    @Autowired
    private SmsService smsService;

    @Test
    void validBelgianNumber() {
        assertTrue(smsService.isValidPhoneNumber("+32475123456", "+32"));
        assertTrue(smsService.isValidPhoneNumber("+32499123456", "+32"));
    }

    @Test
    void invalidBelgianNumber_WrongFormat() {
        assertFalse(smsService.isValidPhoneNumber("0475123456", "+32")); // missing +
        assertFalse(smsService.isValidPhoneNumber("+3247ABCD456", "+32")); // letters inside
        assertFalse(smsService.isValidPhoneNumber("+324", "+32")); // too short
        assertFalse(smsService.isValidPhoneNumber("+324751234567891234", "+32")); // too long
    }

    @Test
    void invalidBelgianNumber_WrongCountry() {
        assertFalse(smsService.isValidPhoneNumber("+33612345678", "+32")); // French number
        assertFalse(smsService.isValidPhoneNumber("+4915123456789", "+32")); // German number
    }

    @Test
    void validFrenchNumber() {
        assertTrue(smsService.isValidPhoneNumber("+33612345678", "+33"));
    }

    @Test
    void validGermanNumber() {
        assertTrue(smsService.isValidPhoneNumber("+4915123456789", "+49"));
    }

    @Test
    void edgeCases() {
        assertFalse(smsService.isValidPhoneNumber(null, "+32"));
        assertFalse(smsService.isValidPhoneNumber("", "+32"));
        assertFalse(smsService.isValidPhoneNumber("     ", "+32"));
    }
}