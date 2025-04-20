package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsDTO {
    private boolean success;
    private String merchantName;
    private String amount;
    private String maskedCardNumber;
    private String formattedDate;
    private Long transactionId;
}