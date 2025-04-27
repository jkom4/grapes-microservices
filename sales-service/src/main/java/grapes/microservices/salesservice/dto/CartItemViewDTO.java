package grapes.microservices.salesservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representing a single item in the user's cart.
 * <p>
 * This object is used inside the {@link CartResponseDTO} to display detailed
 * information about each product added to a temporary order (cart).
 * </p>
 *
 * <p>
 * Each item includes:
 * <ul>
 *     <li>{@code articleId} – the unique identifier of the product</li>
 *     <li>{@code articleName} – the name of the product (for user-friendly display)</li>
 *     <li>{@code picturePath} – optional image path of the product</li>
 *     <li>{@code quantityKg} – the quantity in kilograms (if sold by weight)</li>
 *     <li>{@code quantity} – the quantity in units (if sold by piece)</li>
 *     <li>{@code price} – the unit price used (depending on kg or unit)</li>
 * </ul>
 * </p>
 *
 * <p>
 * The total price of the entire cart is handled in {@link CartResponseDTO}.
 * </p>
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

