package grapes.microservices.paymentbackend.services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;

import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for SmsService
 * Tests Twilio initialization and OTP SMS sending functionality in different environments
 */
@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsService smsService;

    // Static mocks for Twilio SDK
    private MockedStatic<Twilio> twilioMockedStatic;
    private MockedStatic<Message> messageMockedStatic;

    // Test constants
    private final String FAKE_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private final String FAKE_TOKEN = "authtoken";
    private final String FAKE_FROM_NUMBER = "+15005550006";
    private final String TARGET_PHONE_NUMBER = "+32470123456";
    private final String OTP_CODE = "123456";

    @BeforeEach
    void setUp() {
        // Initialize static mocks before each test
        twilioMockedStatic = mockStatic(Twilio.class);
        messageMockedStatic = mockStatic(Message.class);

        // Configure default settings through reflection
        ReflectionTestUtils.setField(smsService, "accountSid", FAKE_SID);
        ReflectionTestUtils.setField(smsService, "authToken", FAKE_TOKEN);
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", FAKE_FROM_NUMBER);
    }

    @AfterEach
    void tearDown() {
        // Close static mocks to prevent memory leaks
        twilioMockedStatic.close();
        messageMockedStatic.close();
    }

    // --- Twilio Initialization Tests ---

    /**
     * Tests that Twilio does not initialize in development environment
     */
    @Test
    void initTwilio_inDevEnvironment_shouldSkipInitialization() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "development");

        // Act
        smsService.initTwilio();

        // Assert
        twilioMockedStatic.verify(() -> Twilio.init(anyString(), anyString()), never());
        assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"),
                "Twilio should not be initialized in dev environment");
    }

    /**
     * Tests that Twilio does not initialize in test environment
     */
    @Test
    void initTwilio_inTestEnvironment_shouldSkipInitialization() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "test");

        // Act
        smsService.initTwilio();

        // Assert
        twilioMockedStatic.verify(() -> Twilio.init(anyString(), anyString()), never());
        assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"),
                "Twilio should not be initialized in test environment");
    }

    /**
     * Tests that Twilio initializes properly in production environment with valid credentials
     */
    @Test
    void initTwilio_inProdEnvironment_withCredentials_shouldInitialize() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");

        // Act
        smsService.initTwilio();

        // Assert
        twilioMockedStatic.verify(() -> Twilio.init(FAKE_SID, FAKE_TOKEN));
        assertTrue((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"),
                "Twilio should be initialized in production environment");
    }

    /**
     * Tests that Twilio does not initialize in production when SID is missing
     */
    @Test
    void initTwilio_inProdEnvironment_missingSid_shouldNotInitialize() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");
        ReflectionTestUtils.setField(smsService, "accountSid", ""); // Empty SID

        // Act
        smsService.initTwilio();

        // Assert
        twilioMockedStatic.verify(() -> Twilio.init(anyString(), anyString()), never());
        assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"),
                "Twilio should not initialize with missing SID");
    }

    /**
     * Tests that initialization errors are handled properly
     */
    @Test
    void initTwilio_inProdEnvironment_initializationThrowsError_shouldHandle() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");
        twilioMockedStatic.when(() -> Twilio.init(anyString(), anyString()))
                .thenThrow(new RuntimeException("Twilio Init Error"));

        // Act
        smsService.initTwilio();

        // Assert
        twilioMockedStatic.verify(() -> Twilio.init(FAKE_SID, FAKE_TOKEN));
        assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"),
                "Twilio should not be marked as initialized when init fails");
    }

    // --- OTP SMS Sending Tests ---

    /**
     * Tests that SMS sending is skipped in development environment
     */
    @Test
    void sendOtp_inDevEnvironment_shouldLogAndReturn() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "development");
        smsService.initTwilio();

        // Act
        smsService.sendOtp(TARGET_PHONE_NUMBER, OTP_CODE);

        // Assert
        messageMockedStatic.verify(
                () -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()),
                never()
        );
    }

    /**
     * Tests that SMS sending is skipped in test environment
     */
    @Test
    void sendOtp_inTestEnvironment_shouldLogAndReturn() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "test");
        smsService.initTwilio();

        // Act
        smsService.sendOtp(TARGET_PHONE_NUMBER, OTP_CODE);

        // Assert
        messageMockedStatic.verify(
                () -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()),
                never()
        );
    }

    /**
     * Tests that sending OTP fails when Twilio is not initialized in production
     */
    @Test
    void sendOtp_inProdEnvironment_notInitialized_shouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");
        ReflectionTestUtils.setField(smsService, "twilioInitialized", false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            smsService.sendOtp(TARGET_PHONE_NUMBER, OTP_CODE);
        });
        assertTrue(exception.getMessage().contains("not configured or failed to initialize"),
                "Exception should mention initialization failure");
    }

    /**
     * Tests that sending OTP fails when 'from' phone number is missing
     */
    @Test
    void sendOtp_inProdEnvironment_missingFromNumber_shouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", "");
        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            smsService.sendOtp(TARGET_PHONE_NUMBER, OTP_CODE);
        });
        assertTrue(exception.getMessage().contains("Twilio 'from' phone number is missing"),
                "Exception should mention missing 'from' number");
    }

    /**
     * Tests that sending OTP fails when target phone number is missing
     */
    @Test
    void sendOtp_inProdEnvironment_missingToNumber_shouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "production");
        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        // Act & Assert - Test with null phone number
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            smsService.sendOtp(null, OTP_CODE);
        });
        assertTrue(exception1.getMessage().contains("Target phone number for OTP is missing"),
                "Exception should mention missing target number (null case)");

        // Act & Assert - Test with empty phone number
        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            smsService.sendOtp("", OTP_CODE);
        });
        assertTrue(exception2.getMessage().contains("Target phone number for OTP is missing"),
                "Exception should mention missing target number (empty case)");
    }
}