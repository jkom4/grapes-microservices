package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private Long clientId;
    private String cardNumber;
    private String maskedCardNumber;
    private String expirationDate;
    private String cardholderName;
    private String cardType;
    private String status;
    private LocalDate addedDate;
    private String cvv;
}