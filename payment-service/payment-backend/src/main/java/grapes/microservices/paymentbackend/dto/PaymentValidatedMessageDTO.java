package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidatedMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String clientName;
    private Long clientId;
    private String accountNumber;
    private String cardType;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String maritalStatus;
    private BigDecimal averageMonthlySalary;
    private Long transactionId;
    private LocalDateTime transactionDateTime;
    private String debtorBankName;
    private BigDecimal transferAmount;

}