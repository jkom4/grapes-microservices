package grapes.microservices.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidatedMessageDTO {
    private Integer orderId;
    private String clientName; // "user_JSmith_789"
    private Long clientId;
    private Long transactionId;
    private LocalDateTime transactionDateTime;
    private String cardType;         // Visa / Mastercard...
    private BigDecimal transferAmount;
    private String currency;
    private String paymentStatus;    // "Success"
    private String deliveryStatus;   // "Pending"
    private Integer deliveryTimeDays;
    private String address;
    private String phoneNumber;
    private String customerName;
}