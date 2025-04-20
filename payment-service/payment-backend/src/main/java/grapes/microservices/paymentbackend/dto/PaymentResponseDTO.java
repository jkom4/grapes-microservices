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
    private PaymentStatus status;
    private BigDecimal amount;
    private String merchantName;
    private LocalDateTime timestamp;
    private String message;
    private String maskedCardNumber;

    public enum PaymentStatus {
        PENDING,
        INITIATED,
        COMPLETED,
        FAILED,
        SUCCESS,
        ERROR
    }


}