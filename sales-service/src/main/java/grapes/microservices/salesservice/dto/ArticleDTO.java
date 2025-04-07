package grapes.microservices.salesservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTO {

    private Integer id;

    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer familyId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price per kg must be positive")
    private BigDecimal priceKg;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price per unit must be positive")
    private BigDecimal priceUnit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Stock in kg must be positive")
    private BigDecimal stockKg;

    @DecimalMin(value = "0.0", inclusive = true, message = "Stock in units must be positive")
    private BigDecimal stockUnit;

    private String origin;

    private String picturePath;
}