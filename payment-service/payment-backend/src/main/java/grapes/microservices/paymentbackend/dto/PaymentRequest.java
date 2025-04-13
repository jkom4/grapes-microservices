package grapes.microservices.paymentbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull
    @DecimalMin(value = "44.99", inclusive = true)
    private Double amount;
    private String merchant;
    private String cardNumber;
    private String cardExpiration;
    private String cvv;
}