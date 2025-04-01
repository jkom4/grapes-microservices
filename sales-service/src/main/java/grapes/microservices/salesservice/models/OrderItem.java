package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "article_id", nullable = false)
    private Integer articleId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "cart_id")
    private Integer cartId;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity_kg", precision = 10, scale = 2)
    private BigDecimal quantityKg;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "is_scanned", nullable = false)
    private boolean isScanned;
}
