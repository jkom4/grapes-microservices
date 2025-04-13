package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank; // @NotBlank est pour les Strings
import jakarta.validation.constraints.NotNull; // Utilisez @NotNull pour les objets/nombres
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number format")
    private String cardNumber;

    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/20[2-9][0-9]$", message = "Expiration date should be in MM/YYYY format")
    private String expirationDate;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Cardholder name is required")
    @Size(min = 2, max = 100, message = "Cardholder name must be between 2 and 100 characters")
    private String cardholderName;

    // MODIFICATION ICI: Remplacer @NotBlank par @NotNull
    // @NotBlank(message = "Amount is required") // Incorrect pour BigDecimal
    @NotNull(message = "Amount is required")   // Correct pour BigDecimal (vérifie la non-nullité)
    // Vous pourriez ajouter d'autres contraintes si nécessaire, comme @DecimalMin("0.01")
    private BigDecimal amount;
}