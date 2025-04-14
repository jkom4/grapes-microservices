// --- START OF payment-backend/src/main/java/grapes/microservices/paymentbackend/dto/PaymentRequestDTO.java ---
package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotNull(message = "Amount is required") // Le montant peut être initialisé par le formulaire OU par la session
    private BigDecimal amount;

    // --- AJOUT ---
    private String merchantName; // Pour stocker le nom du marchand récupéré de la session ou un défaut
    // -------------


}
// --- END OF payment-backend/src/main/java/grapes/microservices/paymentbackend/dto/PaymentRequestDTO.java ---