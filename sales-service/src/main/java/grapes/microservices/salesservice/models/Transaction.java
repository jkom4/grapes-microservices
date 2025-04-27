package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private Integer orderId;
    private Long userId;
    private String bankTransactionId;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String paymentStatus;
    private String deliveryStatus;
    private Integer deliveryTimeDays;
    private LocalDateTime transactionDateTime;
}