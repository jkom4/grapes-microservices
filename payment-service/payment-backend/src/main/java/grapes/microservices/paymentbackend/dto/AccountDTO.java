package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String accountNumber;
    private Long clientId;
    private String bankName;
    private String authenticationType;
    private BigDecimal balance;
    private LocalDate openingDate;
    private String accountType;
    private String status;
}