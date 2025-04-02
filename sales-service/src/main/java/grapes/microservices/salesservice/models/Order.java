package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "`order`") // mot-clé réservé, donc entouré de backticks
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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "facture_path")
    private String facturePath;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_paid")
    private boolean isPaid;

    @Column(name = "is_finished")
    private boolean isFinished;

    /**
     * One order can have multiple order items.
     * Mapped by the "order" field inside OrderItem.
     * Cascade ensures child entities are saved/removed accordingly.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
}
