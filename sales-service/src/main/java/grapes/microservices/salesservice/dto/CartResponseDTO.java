package grapes.microservices.salesservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO used to return all items in the cart along with the total price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {
    private List<CartItemViewDTO> items;
    private BigDecimal totalPrice;
}
