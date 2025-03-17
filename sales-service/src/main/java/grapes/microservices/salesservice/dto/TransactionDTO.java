package grapes.microservices.salesservice.dto;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private Long id;
    private String clientId;
    private Double amount;
    private LocalDateTime transactionDate;
    private String status;
}
