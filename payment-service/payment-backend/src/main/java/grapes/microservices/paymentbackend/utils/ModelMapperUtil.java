package grapes.microservices.paymentbackend.utils;

import grapes.microservices.paymentbackend.dto.*;
import grapes.microservices.paymentbackend.models.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility for mapping data model objects to DTOs and vice-versa.
 * Provides conversion methods between domain entities and data transfer objects.
 */
@Component
public class ModelMapperUtil {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Converts a Client entity to ClientDTO.
     * Sensitive fields like password and nationalRegistryNumber are not mapped.
     *
     * @param client The client entity to convert
     * @return The corresponding DTO or null if client is null
     */
    public ClientDTO toClientDTO(Client client) {
        if (client == null) return null;

        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setPhoneNumber(client.getPhoneNumber());
        dto.setBirthDate(client.getBirthDate());
        dto.setAddress(client.getAddress());
        dto.setGender(client.getGender());
        dto.setMaritalStatus(client.getMaritalStatus());
        dto.setAverageMonthlySalary(client.getAverageMonthlySalary());
        // Password and sensitive data are never mapped
        return dto;
    }

    /**
     * Converts a list of Client entities to a list of ClientDTOs.
     *
     * @param clients The list of client entities to convert
     * @return List of corresponding DTOs or null if input is null
     */
    public List<ClientDTO> toClientDTOList(List<Client> clients) {
        if (clients == null) return null;
        return clients.stream().map(this::toClientDTO).collect(Collectors.toList());
    }

    /**
     * Converts an Account entity to AccountDTO.
     *
     * @param account The account entity to convert
     * @return The corresponding DTO or null if account is null
     */
    public AccountDTO toAccountDTO(Account account) {
        if (account == null) return null;

        AccountDTO dto = new AccountDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setClientId(account.getClient().getId());
        dto.setBankName(account.getBank().getBankName());
        dto.setAuthenticationType(account.getAuthenticationType());
        dto.setBalance(account.getBalance());
        dto.setOpeningDate(account.getOpeningDate());
        dto.setAccountType(account.getAccountType());
        dto.setStatus(account.getStatus());
        return dto;
    }

    /**
     * Converts a list of Account entities to a list of AccountDTOs.
     *
     * @param accounts The list of account entities to convert
     * @return List of corresponding DTOs or null if input is null
     */
    public List<AccountDTO> toAccountDTOList(List<Account> accounts) {
        if (accounts == null) return null;
        return accounts.stream().map(this::toAccountDTO).collect(Collectors.toList());
    }

    /**
     * Converts a Card entity to CardDTO with number masking for security.
     * Full card number is never transferred to the DTO.
     *
     * @param card The card entity to convert
     * @return The corresponding DTO or null if card is null
     */
    public CardDTO toCardDTO(Card card) {
        if (card == null) return null;

        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setClientId(card.getClient().getId());
        dto.setCardNumber(null); // Never transfer full number
        dto.setMaskedCardNumber(card.getMaskedCardNumber());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setCardholderName(card.getCardholderName());
        dto.setCardType(card.getCardType());
        dto.setStatus(card.getStatus());
        dto.setAddedDate(card.getAddedDate());
        // CVV is not mapped from entity
        return dto;
    }

    /**
     * Converts a list of Card entities to a list of CardDTOs.
     *
     * @param cards The list of card entities to convert
     * @return List of corresponding DTOs or null if input is null
     */
    public List<CardDTO> toCardDTOList(List<Card> cards) {
        if (cards == null) return null;
        return cards.stream().map(this::toCardDTO).collect(Collectors.toList());
    }

    /**
     * Converts a TransactionEntity to TransactionDTO.
     *
     * @param transaction The transaction entity to convert
     * @return The corresponding DTO or null if transaction is null
     */
    public TransactionDTO toTransactionDTO(TransactionEntity transaction) {
        if (transaction == null) return null;

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setDebtorAccount(transaction.getDebtorAccount());
        dto.setCreditorAccount(transaction.getCreditorAccount());
        dto.setDebtorBank(transaction.getDebtorBank());
        dto.setCreditorBank(transaction.getCreditorBank());
        dto.setClientId(transaction.getClientId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setClientAccountNumber(transaction.getClientAccountNumber());
        dto.setTransactionDateTime(transaction.getTransactionDateTime());
        dto.setTransferAmount(transaction.getTransferAmount());
        dto.setMerchantName(transaction.getMerchantName());
        dto.setMerchantBusinessSector(transaction.getMerchantBusinessSector());
        dto.setAuthenticationType3DS(transaction.getAuthenticationType3DS());
        dto.setStatus3DS(transaction.getStatus3DS());
        // Message and communication are not in the entity
        dto.setDebtorAccountNewBalance(transaction.getDebtorAccountNewBalance());
        dto.setCreditorAccountNewBalance(transaction.getCreditorAccountNewBalance());
        dto.setStatus(transaction.getStatus());
        return dto;
    }

    /**
     * Converts a TransactionEntity to a simplified PaymentResponseDTO.
     *
     * @param transaction The transaction entity to convert
     * @return The corresponding simplified DTO or null if transaction is null
     */
    public PaymentResponseDTO toPaymentResponseDTO(TransactionEntity transaction) {
        if (transaction == null) return null;

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setTransactionId(transaction.getId());
        dto.setStatus(transaction.getStatus());
        dto.setAmount(transaction.getTransferAmount());
        dto.setMerchantName(transaction.getMerchantName());
        dto.setTimestamp(transaction.getTransactionDateTime());
        // Message is not in the base entity

        // Mask account number
        if (transaction.getClientAccountNumber() != null && transaction.getClientAccountNumber().length() > 4) {
            String accountNumber = transaction.getClientAccountNumber();
            String masked = "XXXX" + accountNumber.substring(accountNumber.length() - 4);
            dto.setMaskedCardNumber(masked);
        } else {
            dto.setMaskedCardNumber("XXXX");
        }

        return dto;
    }

    /**
     * Creates a PaymentDetailsDTO from transaction information.
     *
     * @param merchantName The name of the merchant
     * @param amount The transaction amount
     * @param cardNumber The card number (will be masked)
     * @param transactionId The transaction ID
     * @return A new PaymentDetailsDTO with formatted data
     */
    public PaymentDetailsDTO createPaymentDetailsDTO(String merchantName, BigDecimal amount, String cardNumber, Long transactionId) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();
        dto.setSuccess(true);
        dto.setMerchantName(merchantName);
        dto.setAmount(String.format(Locale.FRANCE, "EUR %.2f", amount));
        dto.setMaskedCardNumber(maskCardNumber(cardNumber));
        dto.setFormattedDate(dateTimeFormatter.format(java.time.LocalDateTime.now()));
        dto.setTransactionId(transactionId);
        return dto;
    }

    /**
     * Masks a card number for display, showing only the last 4 digits.
     *
     * @param cardNumber The full card number to mask
     * @return The masked card number
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null) return "XXXX-XXXX-XXXX-XXXX";
        int maskLength = Math.max(0, cardNumber.length() - 4);
        String mask = "X".repeat(maskLength);
        String lastFour = cardNumber.substring(maskLength);

        if (cardNumber.length() < 4) return cardNumber;
        return "XXXXXXXXXXXX" + cardNumber.substring(cardNumber.length() - 4);
    }
}