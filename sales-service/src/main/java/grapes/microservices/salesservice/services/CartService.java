package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.CartItemViewDTO;
import grapes.microservices.salesservice.dto.CartResponseDTO;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import grapes.microservices.salesservice.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing the shopping cart logic.
 * It handles adding articles to the cart by validating quantities,
 * ensuring stock availability, and persisting order items.
 */
@Service
public class CartService {

    private final ArticleRepository articleRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;


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
        BigDecimal price;
        if (item.getQuantityKg() != null && item.getQuantityKg().compareTo(BigDecimal.ZERO) > 0) {
            price = article.getPriceKg();
        } else {
            price = article.getPriceUnit();
        }

        item.setPrice(price);
        item.setScanned(false);

        return orderItemRepository.save(item);
    }


    public CartResponseDTO getCartContent(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune commande trouvée avec l'ID: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        List<CartItemViewDTO> cartItems = items.stream().map(item -> {
            Article article = articleRepository.findById(item.getArticleId())
                    .orElse(null);

            return CartItemViewDTO.builder()
                    .id(item.getId())
                    .articleId(item.getArticleId())
                    .articleName(article != null ? article.getName() : "Article inconnu")
                    .picturePath(article != null ? article.getPicturePath() : null)
                    .quantityKg(item.getQuantityKg())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = cartItems.stream()
                .map(i -> i.getPrice().multiply(i.getQuantityKg() != null ? i.getQuantityKg() :
                        (i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cartItems, total.setScale(2, RoundingMode.HALF_UP));
    }





    public void removeFromCart(Integer orderId, Integer itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        if (!item.getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("This item does not belong to order ID: " + orderId);
        }

        orderItemRepository.deleteById(itemId);
    }


    @Transactional
    public String clearCart(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is already empty or does not exist.");
        }

        orderItemRepository.deleteAll(items);

        return "Cart cleared successfully for order ID: " + orderId;
    }
    }
