package grapes.microservices.paymentbackend.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import grapes.microservices.paymentbackend.utils.KeystoreUtils;
import grapes.microservices.paymentbackend.utils.SignUtils;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AcsService
 * Focuses on testing the payment processing functionality with various client and card states
 */
@ExtendWith(MockitoExtension.class)
class AcsServiceTest {

    @InjectMocks
    private AcsService acsService;

    // Mock dependencies
    @Mock private TransactionEntity mockTransaction;
    @Mock private Client mockClient;
    @Mock private Card mockCard;
    @Mock private PrivateKey mockPrivateKey;

    // Static mocks declaration
    private static MockedStatic<KeystoreUtils> keystoreUtilsMockedStatic;
    private static MockedStatic<SignUtils> signUtilsMockedStatic;

    // Test constants
    private final String DUMMY_CARD_NUMBER = "1234567890123456";
    private final String DUMMY_EXP_DATE = "12/2025";
    private final BigDecimal DUMMY_AMOUNT = new BigDecimal("99.99");
    private final String DUMMY_MERCHANT = "TestMerchant";
    private final String DUMMY_SIGNATURE = "fakeSignature";

    @BeforeEach
    void setUp() {
        // Close any potentially existing static mocks
        closeStaticMocks();

        // Initialize static mocks without any 'when' conditions here
        keystoreUtilsMockedStatic = mockStatic(KeystoreUtils.class);
        signUtilsMockedStatic = mockStatic(SignUtils.class);

        // Inject @Value properties required by AcsService
        ReflectionTestUtils.setField(acsService, "clientKeystorePath", "dummy/path.jks");
        ReflectionTestUtils.setField(acsService, "clientKeystorePassword", "dummy");
        ReflectionTestUtils.setField(acsService, "clientKeystoreAlias", "dummy");
        ReflectionTestUtils.setField(acsService, "clientKeyPassword", "dummy");
        ReflectionTestUtils.setField(acsService, "acsPort", 8081);
        ReflectionTestUtils.setField(acsService, "clientTruststorePathForAcs", "dummy/trust.jks");
        ReflectionTestUtils.setField(acsService, "clientTruststorePasswordForAcs", "dummy");
        ReflectionTestUtils.setField(acsService, "acsTrustedAlias", "dummy_acs");
    }

    @AfterEach
    void tearDown() {
        closeStaticMocks();
    }

    /**
     * Helper method to safely close static mocks
     */
    private void closeStaticMocks() {
        if (keystoreUtilsMockedStatic != null && !keystoreUtilsMockedStatic.isClosed()) keystoreUtilsMockedStatic.close();
        if (signUtilsMockedStatic != null && !signUtilsMockedStatic.isClosed()) signUtilsMockedStatic.close();
    }

    // --- Simplified Tests (with local stubbing) ---

    /**
     * Tests the scenario where keystore access fails during payment processing
     * Expected: The method should return false when unable to access the keystore
     */
    @Test
    void processPayment_ClientHasCard_FailsOnKeystoreAccessAndReturnsFalse() throws Exception {
        // Arrange - Set up all necessary behaviors HERE
        when(mockClient.getCards()).thenReturn(List.of(mockCard));
        when(mockCard.getCardNumber()).thenReturn(DUMMY_CARD_NUMBER);
        when(mockCard.getExpirationDate()).thenReturn(DUMMY_EXP_DATE);
        when(mockTransaction.getTransferAmount()).thenReturn(DUMMY_AMOUNT); // Required for dataToSign
        when(mockTransaction.getMerchantName()).thenReturn(DUMMY_MERCHANT); // Required for dataToSign

        // KeystoreUtils behavior for this test (expected failure)
        keystoreUtilsMockedStatic.when(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new java.io.FileNotFoundException("dummy/path.jks (simulated)")); // Simulate the exact error seen in logs

        // Act
        boolean result = acsService.processPayment(mockTransaction, mockClient);

        // Assert
        assertFalse(result, "processPayment should return false when KeystoreUtils call fails");

        // Verify
        verify(mockClient, times(1)).getCards();
        verify(mockCard, times(1)).getCardNumber();
        verify(mockCard, times(1)).getExpirationDate();
        // Verify attempt to call getPrivateKey
        keystoreUtilsMockedStatic.verify(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()), times(1));
        // Verify signature was NOT attempted because previous step failed
        signUtilsMockedStatic.verify(() -> SignUtils.signData(anyString(), any()), never());
    }

    /**
     * Tests the scenario where the client has no payment cards
     * Expected: The method should return false early without attempting keystore access
     */
    @Test
    void processPayment_ClientHasNoCards_ReturnsFalseEarly() {
        // Arrange - Set specific behavior
        when(mockClient.getCards()).thenReturn(Collections.emptyList());

        // Act
        boolean result = acsService.processPayment(mockTransaction, mockClient);

        // Assert
        assertFalse(result, "processPayment should return false early if client has no cards");

        // Verify (No calls to Keystore/Sign utilities)
        keystoreUtilsMockedStatic.verify(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()), never());
        signUtilsMockedStatic.verify(() -> SignUtils.signData(anyString(), any()), never());
    }

    /**
     * Tests the scenario where the client's card list is null
     * Expected: The method should return false early without attempting keystore access
     */
    @Test
    void processPayment_ClientCardsIsNull_ReturnsFalseEarly() {
        // Arrange - Set specific behavior
        when(mockClient.getCards()).thenReturn(null);

        // Act
        boolean result = acsService.processPayment(mockTransaction, mockClient);

        // Assert
        assertFalse(result, "processPayment should return false early if client card list is null");

        // Verify
        keystoreUtilsMockedStatic.verify(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()), never());
        signUtilsMockedStatic.verify(() -> SignUtils.signData(anyString(), any()), never());
    }

    /**
     * Tests the scenario where card details are missing
     * Expected: The method should return false early without attempting keystore access
     */
    @Test
    void processPayment_CardDetailsMissing_ReturnsFalseEarly() {
        // Arrange - Set specific behavior
        // Client has a list of cards (configured in mockClient)
        when(mockClient.getCards()).thenReturn(List.of(mockCard));
        when(mockCard.getCardNumber()).thenReturn(null); // But the card has no number

        // Act
        boolean result = acsService.processPayment(mockTransaction, mockClient);

        // Assert
        assertFalse(result, "processPayment should return false early if card number is null");

        // Verify
        keystoreUtilsMockedStatic.verify(() -> KeystoreUtils.getPrivateKey(anyString(), anyString(), anyString(), anyString()), never());
        signUtilsMockedStatic.verify(() -> SignUtils.signData(anyString(), any()), never());
    }
}