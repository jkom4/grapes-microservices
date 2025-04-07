package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "delivery_status_id", nullable = false)
    private Integer deliveryStatusId;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "comment")
    private String comment;

    @Column(name = "doorstep")
    private Boolean doorstep;

    @Lob
    @Column(name = "signature")
    private byte[] signature;
}
