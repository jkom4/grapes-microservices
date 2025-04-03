package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.CartItemViewDTO;
import grapes.microservices.salesservice.dto.CartResponseDTO;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class responsible for managing the shopping cart logic.
 * It handles adding articles to the cart by validating quantities,
 * ensuring stock availability, and persisting order items.
 */
@Service
public class CartService {

    private final ArticleRepository articleRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Constructor for CartService.
     *
     * @param articleRepository the repository used to access articles
     * @param orderItemRepository the repository used to manage order items
     */
    public CartService(ArticleRepository articleRepository, OrderItemRepository orderItemRepository) {
        this.articleRepository = articleRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Adds an article to the user's cart (associated to a specific orderId).
     * It checks stock availability for both unit and kg-based quantities
     * and calculates the price based on the type of quantity.
     *
     * @param request the DTO containing articleId, orderId, and quantity info
     * @return the created {@link OrderItem} entity saved in the database
     * @throws IllegalArgumentException if the article doesn't exist or if stock is insufficient
     */
    public OrderItem addToCart(OrderItem item) {
        Article article = articleRepository.findById(item.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("Article not found with ID: " + item.getArticleId()));

        // Stock validation
        if (item.getQuantityKg() != null && item.getQuantityKg().compareTo(article.getStockKg()) > 0) {
            throw new IllegalArgumentException("Not enough stock in kg for article: " + article.getName());
        }

        if (item.getQuantity() != null && item.getQuantity().compareTo(article.getStockUnit()) > 0) {
            throw new IllegalArgumentException("Not enough stock in unit for article: " + article.getName());
        }

        // Set price according to quantity type
        BigDecimal price = item.getQuantityKg() != null ? article.getPriceKg() : article.getPriceUnit();

        item.setPrice(price);
        item.setScanned(false);

        return orderItemRepository.save(item);
    }


    public CartResponseDTO getCartContent(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // 🧠 Mapping OrderItem + fetching article name
        List<CartItemViewDTO> enrichedItems = items.stream()
                .map(item -> {
                    String name = articleRepository.findById(item.getArticleId())
                            .map(Article::getName)
                            .orElse("Unknown");

                    return CartItemViewDTO.builder()
                            .id(item.getId())
                            .articleId(item.getArticleId())
                            .articleName(name) // 🟢 Injected manually here
                            .quantityKg(item.getQuantityKg())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build();
                })
                .toList();

        // 💰 Total price calculation
        BigDecimal total = enrichedItems.stream()
                .map(i -> {
                    BigDecimal qty = i.getQuantityKg() != null ? i.getQuantityKg() : i.getQuantity();
                    return i.getPrice().multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .items(enrichedItems)
                .totalPrice(total)
                .build();
    }


    public void removeFromCart(Integer itemId) {
        if (!orderItemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Cart item not found with ID: " + itemId);
        }
        orderItemRepository.deleteById(itemId);
    }

    public void clearCart(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is already empty or does not exist.");
        }

        orderItemRepository.deleteAll(items);
    }



}
