package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.CartRequestDTO;
import grapes.microservices.salesservice.dto.CartResponseDTO;
import grapes.microservices.salesservice.dto.CreateOrderRequestDTO;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.services.CartService;
import grapes.microservices.salesservice.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

/**
 * Controller responsible for handling shopping cart operations,
 * including creating temporary orders (carts) and adding items to them.
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    /**
     * Constructor for CartController.
     *
     * @param cartService  service handling cart operations (adding items)
     * @param orderService service handling order creation
     */
    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    /**
     * Adds an article to the user's cart (represented as a temporary order).
     * The item is linked to an existing orderId.
     *
     * @param request the DTO containing the orderId, articleId, and quantity
     * @return the saved {@link OrderItem} representing the cart item
     */
    @PostMapping("/add")
    public ResponseEntity<OrderItem> addToCart(@RequestBody CartRequestDTO request) {
        OrderItem item = cartService.addToCart(request);
        return ResponseEntity.ok(item);
    }

    /**
     * Initializes a new empty cart by creating a temporary order
     * with isPaid = false and isFinished = false.
     *
     * @param request the DTO containing the userId
     * @return the newly created {@link Order} representing the cart
     */
    @PostMapping("/init")
    public ResponseEntity<Order> initCart(@RequestBody CreateOrderRequestDTO request) {
        Order newOrder = orderService.createTemporaryOrder(request.getUserId());
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Integer orderId) {
        CartResponseDTO cart = cartService.getCartContent(orderId);
        return ResponseEntity.ok(cart);
    }


    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Integer itemId) {
        try {
            cartService.removeFromCart(itemId);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/clear/{orderId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer orderId) {
        try {
            cartService.clearCart(orderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<?> simulatePayment(@PathVariable Integer orderId) {
        try {
            orderService.finalizePaymentAndClearCart(orderId);
            return ResponseEntity.ok("Payment confirmed, stock updated and cart cleared.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Integer id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }




}
