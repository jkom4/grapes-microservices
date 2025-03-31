package grapes.microservices.salesservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "family_id", nullable = false)
    private Integer familyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_kg", precision = 10, scale = 2)
    private BigDecimal priceKg;

    @Column(name = "price_unit", precision = 10, scale = 2)
    private BigDecimal priceUnit;

    @Column(name = "stock_kg", precision = 10, scale = 2)
    private BigDecimal stockKg;

    @Column(name = "unit_stock", precision = 10, scale = 2)
    private BigDecimal stockUnit;

    @Column(length = 100)
    private String origin;

    @Column(name = "picture_path", length = 255)
    private String picturePath;
}
