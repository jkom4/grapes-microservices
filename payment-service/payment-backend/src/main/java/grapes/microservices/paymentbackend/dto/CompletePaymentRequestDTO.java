package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletePaymentRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentToken;
    private Long transactionId;
}