package grapes.microservices.salesservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO used to return all items in the cart along with the total price.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemViewDTO {
    private Integer id;
    private Integer articleId;
    private String articleName;
    private String picturePath;
    private BigDecimal quantityKg;
    private BigDecimal quantity;
    private BigDecimal price;
}
