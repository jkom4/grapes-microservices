package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponseDTO {
    private boolean success;
    private String message;
    private Long transactionId;
    private String status;

    public static VerificationResponseDTO success(Long transactionId, String message) {
        return new VerificationResponseDTO(true, message, transactionId, "Completed");
    }
    public static VerificationResponseDTO failure(String message) {
        return new VerificationResponseDTO(false, message, null, "Failed");
    }
}