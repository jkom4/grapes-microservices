package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.io.Serializable;
/**
 * DTO representing the data required for a payment request,
 * including card details and amount, with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number format")
    private String cardNumber;

    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/20[2-9][0-9]$", message = "Expiration date should be in MM/YYYY format")
    private String expirationDate;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    // Name of the merchant initiating the payment
    private String merchantName;


}