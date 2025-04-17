package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/* * DTO representing the response after a payment verification attempt.
 * Contains transaction details, status, and message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponseDTO {
    private boolean success;
    private String message;
    private Long transactionId;
    private String status;


    /*
     * Constructor with essential fields (sets status automatically)
     * @param success Indicates if the verification was successful
     * @param message Message providing details about the verification result
     * @param transactionId ID of the transaction being verified
     * @param status Status of the transaction (e.g., "Completed", "Failed")
     */
    public static VerificationResponseDTO success(Long transactionId, String message) {
        return new VerificationResponseDTO(true, message, transactionId, "Completed");
    }

    public static VerificationResponseDTO failure(String message) {
        return new VerificationResponseDTO(false, message, null, "Failed");
    }
}