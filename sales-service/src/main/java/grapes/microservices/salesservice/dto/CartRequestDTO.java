package grapes.microservices.salesservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO used to add an item to a shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequestDTO {
    private Integer orderId;
    private Integer articleId;
    private BigDecimal quantityKg;
    private BigDecimal quantity;
}
