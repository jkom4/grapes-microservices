package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for completing a payment request.
 * This class is used to encapsulate the data required to complete a payment.
 * It includes the payment token and the transaction ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletePaymentRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentToken;
    private Long transactionId;
}