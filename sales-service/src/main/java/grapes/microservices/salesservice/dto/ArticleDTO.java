package grapes.microservices.salesservice.dto;

import jakarta.persistence.Column;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTO {


    @Column(name = "category_id")
    @NotNull private Integer categoryId;


    @Column(name = "family_id")
    @NotNull private Integer familyId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price per kg must be positive")
    @Column(name = "price_kg")
    private BigDecimal priceKg;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price per unit must be positive")
    @Column(name = "price_unit")
    private BigDecimal priceUnit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Stock in kg must be positive")
    @Column(name = "stock_kg")
    private BigDecimal stockKg;

    @DecimalMin(value = "0.0", inclusive = true, message = "Stock in units must be positive")
    @Column(name = "stock_unit")
    private BigDecimal stockUnit;

    private String origin;

    @Column(name = "picture_path")
    private String picturePath;

}
