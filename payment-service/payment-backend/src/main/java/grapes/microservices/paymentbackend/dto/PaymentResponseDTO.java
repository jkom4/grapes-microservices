package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing the response after a payment attempt.
 * Contains transaction details, status, and timestamp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long transactionId;
    private String status; // e.g., "SUCCESS", "FAILED", "PENDING"
    private BigDecimal amount;
    private String merchantName;
    private LocalDateTime timestamp; // Time when the response was generated
    private String message; // e.g., "Payment processed successfully", "Insufficient funds"
    private String maskedCardNumber; // e.g., "************1234"

    // Constructor with essential fields (sets timestamp automatically)
    public PaymentResponseDTO(Long transactionId, String status, BigDecimal amount, String merchantName) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.merchantName = merchantName;
        this.timestamp = LocalDateTime.now(); // Set timestamp on creation
    }
}