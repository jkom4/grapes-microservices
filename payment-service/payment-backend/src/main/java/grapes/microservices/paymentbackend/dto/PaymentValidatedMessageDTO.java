package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidatedMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Client Info
    private String nomClient;
    private Long idClient;
    private String numeroCompte;
    private String cardType;
    private String sexe;
    private LocalDate dateNaissance;
    private Integer age;
    private String statutMarital;
    private BigDecimal salaireMensuelMoyen;
    private Long transactionId;
    private LocalDateTime dateHeureTransaction;
    private String nomBanqueDebiteur;
    private BigDecimal sommeTransferee;

}