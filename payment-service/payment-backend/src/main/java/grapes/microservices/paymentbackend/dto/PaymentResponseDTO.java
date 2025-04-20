package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {


    private Long transactionId;
    private String status;
    private BigDecimal amount;
    private String merchantName;
    private LocalDateTime timestamp;
    private String message;
    private String maskedCardNumber;

    public PaymentResponseDTO(Long transactionId, String status, BigDecimal amount, String merchantName) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.merchantName = merchantName;
        this.timestamp = LocalDateTime.now();
    }
}