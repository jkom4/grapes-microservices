package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`") //Reserved keyword, so enclosed in backticks.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer code;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "facture_path")
    private String facturePath;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_paid")
    private boolean paid;

    @Column(name = "is_finished", nullable = false)
    private boolean finished;
}
