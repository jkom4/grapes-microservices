package grapes.microservices.salesservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object used to add an item to a shopping cart.
 * Contains the order ID (cart), article ID, and optional quantity (in kg or unit).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequestDTO {

    /**
     * ID of the cart (temporary order) the item should be added to.
     */
    private Integer orderId;

    /**
     * ID of the article to add to the cart.
     */
    private Integer articleId;

    /**
     * Quantity of the article in kilograms (if applicable).
     */
    private BigDecimal quantityKg;

    /**
     * Quantity of the article in units (if applicable).
     */
    private BigDecimal quantity;
}
