package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.CompletePaymentRequestDTO;
import grapes.microservices.paymentbackend.dto.PaymentRequestDTO;
import grapes.microservices.paymentbackend.models.Card;
import grapes.microservices.paymentbackend.models.Client;
import grapes.microservices.paymentbackend.models.TransactionEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final PaymentService paymentService;
    private final CardService cardService;
    private final ClientService clientService;
    private final TransactionService transactionService;
    private final CacheManager cacheManager;

    private static final String SESSION_INIT_AMOUNT_KEY = "initialPaymentAmount";
    private static final String SESSION_INIT_MERCHANT_KEY = "initialMerchantName";
    private static final String SESSION_INIT_ORDER_KEY = "initialOrderID";
    private static final String SESSION_INIT_PAYMENT_ID_KEY = "initialPaymentId";
    private static final String SESSION_INIT_REDIRECT_URL_KEY = "initialRedirectUrl";
    private static final String SESSION_CLIENT_ID_KEY = "clientId";

    private static final String PENDING_PAYMENTS_CACHE_NAME = "pendingPayments";

    public ResponseEntity<?> initiatePayment(PaymentRequestDTO paymentRequest, HttpSession session) {
        log.info("[PaymentProcessingService] Initiation du paiement");

        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            log.warn("Client non authentifié (session sans clientId)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.error("ID Client {} non trouvé en base malgré présence en session! Session invalidée.", clientId);
            session.invalidate();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found. Please log in again."));
        }

        Client client = clientOpt.get();

        PaymentRequestDTO finalPaymentRequest = prepareFinalPaymentRequest(paymentRequest, session);

        ResponseEntity<?> validationResult = validatePaymentRequest(finalPaymentRequest, client);
        if (validationResult != null) {
            return validationResult;
        }

        TransactionEntity transaction = transactionService.createPaymentTransaction(finalPaymentRequest, client);
        Long transactionId = transaction.getId();

        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            transactionService.failTransaction(transactionId, "Cache Error during Initiation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        pendingPaymentsCache.put(transactionId, finalPaymentRequest);

        String otpCode = cardService.initiateCardVerification(finalPaymentRequest, client, String.valueOf(transactionId));
        if (otpCode == null) {
            transactionService.failTransaction(transactionId, "ACS Initiation Failed");
            pendingPaymentsCache.evictIfPresent(transactionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message",
                            "Failed to initiate 3D Secure verification with the bank. Please try again later."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Verification required. Check your phone for the OTP code.");
        response.put("transactionId", transactionId);
        return ResponseEntity.ok(response);
    }

    private PaymentRequestDTO prepareFinalPaymentRequest(PaymentRequestDTO paymentRequest, HttpSession session) {
        PaymentRequestDTO finalRequest = paymentRequest;

        BigDecimal initialAmount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String initialMerchant = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);

        if (initialAmount != null) {
            finalRequest.setAmount(initialAmount);
        }

        if (initialMerchant != null) {
            finalRequest.setMerchantName(initialMerchant);
        } else if (finalRequest.getMerchantName() == null || finalRequest.getMerchantName().isEmpty()) {
            finalRequest.setMerchantName("Grapes");
        }

        session.removeAttribute(SESSION_INIT_AMOUNT_KEY);
        session.removeAttribute(SESSION_INIT_MERCHANT_KEY);
        session.removeAttribute(SESSION_INIT_PAYMENT_ID_KEY);


        return finalRequest;
    }

    private ResponseEntity<?> validatePaymentRequest(PaymentRequestDTO request, Client client) {
        try {
            if (!paymentService.validateCardNumber(request.getCardNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Invalid card number format."));
            }

            if (!paymentService.isExpirationDateValid(request.getExpirationDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Card has expired or expiration date format is invalid."));
            }

            List<Card> clientCards = clientService.getClientCards(client.getId());
            boolean cardFound = false;
            for (Card card : clientCards) {
                if (card.getCardNumber().equals(request.getCardNumber())) {
                    cardFound = true;

                    if (!card.getExpirationDate().equals(request.getExpirationDate())) {
                        log.warn("Date d'expiration de carte non concordante. Fournie: {}, Enregistrée: {}",
                                request.getExpirationDate(), card.getExpirationDate());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("success", false, "message", "Card expiration date doesn't match our records."));
                    }
                    break;
                }
            }

            if (!cardFound) {
                log.warn("Carte {} non trouvée pour le client {}",
                        paymentService.maskCardNumber(request.getCardNumber()), client.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "This card is not associated with your account."));
            }

            BigDecimal accountBalance = paymentService.getAccountBalance(client);
            if (accountBalance == null || accountBalance.compareTo(request.getAmount()) < 0) {
                log.warn("Solde insuffisant pour le client {}. Solde: {}, Requis: {}",
                        client.getEmail(), accountBalance, request.getAmount());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Insufficient funds in your account to complete this transaction."));
            }

            if (!paymentService.isAmountAcceptable(request.getAmount())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Payment amount is invalid or exceeds transaction limits."));
            }

            return null;

        } catch (Exception e) {
            log.error("Erreur pendant la validation du paiement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "A technical error occurred during payment validation."));
        }
    }

    public ResponseEntity<?> completePayment(CompletePaymentRequestDTO completeRequest, HttpSession session) {
        Long transactionId = completeRequest.getTransactionId();
        log.info("Finalisation du paiement pour la transaction ID: {}", transactionId);

        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired. Please log in again."));
        }

        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Client associated with session not found."));
        }

        Client client = clientOpt.get();

        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
        if (wrapper == null) {
            return checkTransactionStatus(transactionId);
        }

        PaymentRequestDTO paymentRequest = (PaymentRequestDTO) wrapper.get();

        boolean paymentSuccess = paymentService.processPayment(
                completeRequest.getPaymentToken(),
                paymentRequest,
                client,
                transactionId,
                (Long) session.getAttribute(SESSION_INIT_ORDER_KEY)
        );

        if (paymentSuccess) {
            pendingPaymentsCache.evict(transactionId);

            // Récupérer l'URL de redirection personnalisée
            String redirectUrl = (String) session.getAttribute(SESSION_INIT_REDIRECT_URL_KEY);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("transactionId", transactionId);

            // Ajouter l'URL de redirection à la réponse si elle existe
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.put("redirectUrl", redirectUrl);
            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Verification failed. The code entered is incorrect or has expired."));
        }
    }

    private ResponseEntity<?> checkTransactionStatus(Long transactionId) {
        Optional<TransactionEntity> existingTxOpt = transactionService.findTransactionById(transactionId);
        if (existingTxOpt.isPresent()) {
            String status = existingTxOpt.get().getStatus();
            if ("Completed".equals(status)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false,
                                "message", "This payment has already been processed successfully."));
            } else if ("Failed".equals(status)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false,
                                "message", "This payment request has already failed processing."));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Payment request not found or expired. Please restart the payment process."));
    }

    public ResponseEntity<?> getPendingPaymentDetails(Long transactionId, HttpSession session) {
        log.info("Récupération des détails de paiement en attente pour l'ID: {}", transactionId);

        Long clientId = (Long) session.getAttribute(SESSION_CLIENT_ID_KEY);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Client session not found or expired."));
        }

        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error (Cache Configuration)."));
        }

        Cache.ValueWrapper wrapper = pendingPaymentsCache.get(transactionId);
        if (wrapper == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No pending payment found or it has expired."));
        }

        PaymentRequestDTO pendingPayment = (PaymentRequestDTO) wrapper.get();

        Optional<TransactionEntity> txOpt = transactionService.findTransactionById(transactionId);
        if (txOpt.isEmpty() || !txOpt.get().getClientId().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied to payment details."));
        }

        if (!"Initiated".equals(txOpt.get().getStatus())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("success", false, "message", "This payment is no longer pending verification."));
        }

        String maskedCardNumber = paymentService.maskCardNumber(pendingPayment.getCardNumber());
        String formattedAmount = String.format(Locale.FRANCE, "EUR %.2f", pendingPayment.getAmount());
        String merchantName = pendingPayment.getMerchantName() != null ? pendingPayment.getMerchantName() : "Grapes";

        return ResponseEntity.ok(Map.of(
                "success", true,
                "merchantName", merchantName,
                "amount", formattedAmount,
                "maskedCardNumber", maskedCardNumber
        ));
    }

    public ResponseEntity<?> handleInitiationError(Exception e) {
        log.error("Erreur lors de l'initiation du paiement: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An unexpected error occurred during payment initiation."));
    }

    public ResponseEntity<?> getSessionDetails(HttpSession session) {
        BigDecimal amount = (BigDecimal) session.getAttribute(SESSION_INIT_AMOUNT_KEY);
        String merchantName = (String) session.getAttribute(SESSION_INIT_MERCHANT_KEY);
        String redirectUrl = (String) session.getAttribute(SESSION_INIT_REDIRECT_URL_KEY);

        if (amount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No payment details found in session"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("amount", amount);
        response.put("merchantName", merchantName != null ? merchantName : "Grapes");

        // Ajouter l'URL de redirection si elle existe
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            response.put("redirectUrl", redirectUrl);
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> handleCompletionError(Exception e, Long transactionId) {
        log.error("Erreur lors de la finalisation du paiement pour l'ID {}: {}", transactionId, e.getMessage(), e);

        Cache pendingPaymentsCache = cacheManager.getCache(PENDING_PAYMENTS_CACHE_NAME);
        if (pendingPaymentsCache != null) {
            pendingPaymentsCache.evictIfPresent(transactionId);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An internal error occurred during payment completion."));
    }

    public ResponseEntity<?> handlePendingDetailsError(Exception e) {
        log.error("Erreur lors de la récupération des détails de paiement: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving payment details."));
    }
}