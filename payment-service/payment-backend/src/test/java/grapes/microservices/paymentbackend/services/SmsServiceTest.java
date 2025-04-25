package grapes.microservices.paymentbackend.services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        // Set properties via reflection
        ReflectionTestUtils.setField(smsService, "accountSid", "test_account_sid");
        ReflectionTestUtils.setField(smsService, "authToken", "test_auth_token");
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", "+15551234567");
        ReflectionTestUtils.setField(smsService, "environment", "production");
        ReflectionTestUtils.setField(smsService, "twilioInitialized", false);
    }

    @Test
    void initTwilio_ShouldInitializeTwilio_WhenInProductionEnvironment() {
        // Arrange & Act
        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class)) {
            smsService.initTwilio();

            // Assert
            twilioMock.verify(() -> Twilio.init("test_account_sid", "test_auth_token"));
            assertTrue((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"));
        }
    }

    @Test
    void initTwilio_ShouldSkipInitialization_WhenInDevelopmentEnvironment() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "development");

        // Act
        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class)) {
            smsService.initTwilio();

            // Assert
            twilioMock.verifyNoInteractions();
            assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"));
        }
    }

    @Test
    void initTwilio_ShouldSkipInitialization_WhenInTestEnvironment() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "test");

        // Act
        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class)) {
            smsService.initTwilio();

            // Assert
            twilioMock.verifyNoInteractions();
            assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"));
        }
    }

    @Test
    void initTwilio_ShouldNotInitializeTwilio_WhenCredentialsMissing() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "accountSid", "");
        ReflectionTestUtils.setField(smsService, "authToken", "test_auth_token");

        // Act
        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class)) {
            smsService.initTwilio();

            // Assert
            twilioMock.verifyNoInteractions();
            assertFalse((Boolean) ReflectionTestUtils.getField(smsService, "twilioInitialized"));
        }
    }

    @Test
    void sendOtp_ShouldSendSms_WhenInProductionAndInitialized() {
        // Arrange
        String phoneNumber = "+32123456789";
        String otp = "123456";
        Message mockMessage = mock(Message.class);
        when(mockMessage.getSid()).thenReturn("SM12345");

        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            when(creator.create()).thenReturn(mockMessage);

            messageMock.when(() -> Message.creator(
                            any(PhoneNumber.class),
                            any(PhoneNumber.class),
                            anyString()))
                    .thenReturn(creator);

            // Act
            smsService.sendOtp(phoneNumber, otp);

            // Assert
            messageMock.verify(() ->
                    Message.creator(
                            eq(new PhoneNumber("+32123456789")),
                            eq(new PhoneNumber("+15551234567")),
                            eq("Your Grapes Bank 3D Secure verification code is: 123456")
                    )
            );
        }
    }

    @Test
    void sendOtp_ShouldNotSendSms_WhenInDevelopmentEnvironment() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "development");
        String phoneNumber = "+32123456789";
        String otp = "123456";

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            // Act
            smsService.sendOtp(phoneNumber, otp);

            // Assert
            messageMock.verifyNoInteractions();
        }
    }

    @Test
    void sendOtp_ShouldNotSendSms_WhenInTestEnvironment() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "environment", "test");
        String phoneNumber = "+32123456789";
        String otp = "123456";

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            // Act
            smsService.sendOtp(phoneNumber, otp);

            // Assert
            messageMock.verifyNoInteractions();
        }
    }

    @Test
    void sendOtp_ShouldThrowException_WhenTwilioNotInitialized() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "twilioInitialized", false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.sendOtp("+32123456789", "123456"));

        assertEquals("SMS Service (Twilio) is not configured or failed to initialize.", exception.getMessage());
    }

    @Test
    void sendOtp_ShouldThrowException_WhenTwilioPhoneNumberMissing() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", "");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.sendOtp("+32123456789", "123456"));

        assertEquals("Twilio 'from' phone number is missing.", exception.getMessage());
    }

    @Test
    void sendOtp_ShouldThrowException_WhenTargetPhoneNumberMissing() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.sendOtp(null, "123456"));

        assertEquals("Target phone number for OTP is missing.", exception.getMessage());
    }

    @Test
    void sendOtp_ShouldAddPlusPrefix_WhenPhoneNumberDoesntHaveOne() {
        // Arrange
        String phoneNumber = "32123456789"; // No plus sign
        String otp = "123456";
        Message mockMessage = mock(Message.class);
        when(mockMessage.getSid()).thenReturn("SM12345");

        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            when(creator.create()).thenReturn(mockMessage);

            messageMock.when(() -> Message.creator(
                            any(PhoneNumber.class),
                            any(PhoneNumber.class),
                            anyString()))
                    .thenReturn(creator);

            // Act
            smsService.sendOtp(phoneNumber, otp);

            // Assert - verify it was called with the plus sign added
            messageMock.verify(() ->
                    Message.creator(
                            eq(new PhoneNumber("+32123456789")), // Plus sign added
                            any(PhoneNumber.class),
                            anyString()
                    )
            );
        }
    }

    @Test
    void sendOtp_ShouldThrowException_WhenTwilioApiExceptionOccurs() {
        // Arrange
        String phoneNumber = "+32123456789";
        String otp = "123456";

        ReflectionTestUtils.setField(smsService, "twilioInitialized", true);

        ApiException apiException = mock(ApiException.class);
        when(apiException.getMessage()).thenReturn("Invalid number");
        when(apiException.getCode()).thenReturn(21211);
        when(apiException.getMoreInfo()).thenReturn("https://www.twilio.com/docs/errors/21211");

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            when(creator.create()).thenThrow(apiException);

            messageMock.when(() -> Message.creator(
                            any(PhoneNumber.class),
                            any(PhoneNumber.class),
                            anyString()))
                    .thenReturn(creator);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> smsService.sendOtp(phoneNumber, otp));

            assertTrue(exception.getMessage().contains("Failed to send SMS via Twilio"));
            assertEquals(apiException, exception.getCause());
        }
    }
}