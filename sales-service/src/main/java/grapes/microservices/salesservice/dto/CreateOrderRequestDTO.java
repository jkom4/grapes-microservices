package grapes.microservices.salesservice.dto;

import lombok.*;

/**
 * Data Transfer Object used to initialize a new shopping cart (temporary order).
 * Contains only the user ID who is creating the cart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDTO {

    /**
     * ID of the user creating the cart.
     */
    private Integer userId;
}
