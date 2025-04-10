package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.CartRequestDTO;
import grapes.microservices.salesservice.dto.CartResponseDTO;
import grapes.microservices.salesservice.dto.CreateOrderRequestDTO;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.services.CartService;
import grapes.microservices.salesservice.services.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

/**
 * Controller responsible for handling shopping cart operations,
 * including creating temporary orders (carts), adding/removing items,
 * confirming payment, and retrieving orders.
 */
@RestController
@RequestMapping("/clm/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    /**
     * Adds an article to the user's cart (temporary order).
     */
    @PostMapping("/add")
    @CrossOrigin(origins = "http://localhost:3002")
    @Transactional
    public ResponseEntity<?> addToCart(@RequestBody CartRequestDTO request) {
        try {
            OrderItem item = OrderItem.builder()
                    .orderId(request.getOrderId())
                    .articleId(request.getArticleId())
                    .quantity(request.getQuantity())
                    .quantityKg(request.getQuantityKg())
                    .build();

            OrderItem saved = cartService.addToCart(item);
            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }


    /**
     * Initializes a new cart (temporary order).
     */
    @PostMapping("/init")
    @Transactional
    public ResponseEntity<?> initCart(@RequestBody CreateOrderRequestDTO request) {
        try {
            Order newOrder = orderService.createTemporaryOrder(request.getUserId());
            return ResponseEntity.ok(newOrder);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to initialize cart: " + e.getMessage());
        }
    }

    /**
     * Retrieves the contents of a cart for a given order ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getCart(@PathVariable Integer orderId) {
        try {
            CartResponseDTO cart = cartService.getCartContent(orderId);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch cart: " + e.getMessage());
        }
    }

    /**
     * Removes a specific item from the cart.
     */
    @DeleteMapping("/remove/{itemId}")
    @Transactional
    public ResponseEntity<?> removeItemFromCart(@PathVariable Integer itemId) {
        try {
            cartService.removeFromCart(itemId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to remove item: " + e.getMessage());
        }
    }

    /**
     * Clears all items from a cart by order ID.
     */
    @DeleteMapping("/clear/{orderId}")
    @Transactional
    public ResponseEntity<?> clearCart(@PathVariable Integer orderId) {
        try {
            cartService.clearCart(orderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to clear cart: " + e.getMessage());
        }
    }

    /**
     * Finalizes the payment: verifies stock, updates quantities,
     * generates invoice, and clears cart.
     */
    @PostMapping("/pay/{orderId}")
    @Transactional
    public ResponseEntity<?> simulatePayment(@PathVariable Integer orderId) {
        try {
            orderService.finalizePaymentAndClearCart(orderId);
            return ResponseEntity.ok("Payment confirmed, stock updated and cart cleared.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FileNotFoundException e) {
            return ResponseEntity.internalServerError().body("Invoice file generation failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

}
