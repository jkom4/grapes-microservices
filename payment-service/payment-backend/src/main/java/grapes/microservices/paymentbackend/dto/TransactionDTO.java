package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String debtorAccount;
    private String creditorAccount;
    private String debtorBank;
    private String creditorBank;
    private Long clientId;
    private String transactionType;
    private String clientAccountNumber;
    private LocalDateTime transactionDateTime;
    private BigDecimal transferAmount;
    private String merchantName;
    private String merchantBusinessSector;
    private String authenticationType3DS;
    private String status3DS;
    private String message;
    private String communication;
    private BigDecimal debtorAccountNewBalance;
    private BigDecimal creditorAccountNewBalance;
    private String status;
}