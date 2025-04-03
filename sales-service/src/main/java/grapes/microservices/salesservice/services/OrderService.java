package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import grapes.microservices.salesservice.repositories.OrderRepository;
import grapes.microservices.salesservice.utils.InvoiceGenerator;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ArticleRepository articleRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ArticleRepository articleRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.articleRepository = articleRepository;
    }

    public Order createTemporaryOrder(Integer userId) {
        Order order = Order.builder()
                .userId(userId)
                .isFinished(false)
                .isPaid(false)
                .totalPrice(null)
                .code(new Random().nextInt(999999))
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    public void finalizePaymentAndClearCart(Integer orderId) throws FileNotFoundException {
        Order order = getOrderById(orderId);
        List<OrderItem> items = getValidOrderItems(orderId);

        validateStockForItems(items);
        BigDecimal total = updateStockAndComputeTotal(items);
        order.setTotalPrice(total);

        String pdfPath = InvoiceGenerator.generateInvoice(order, items, articleRepository);
        order.setFacturePath(pdfPath);
        order.setPaid(true);

        orderRepository.save(order);
        orderItemRepository.deleteAll(items);
    }

    private List<OrderItem> getValidOrderItems(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty or already processed.");
        }
        return items;
    }

    private void validateStockForItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            Article article = articleRepository.findById(item.getArticleId())
                    .orElseThrow(() -> new IllegalArgumentException("Article not found: " + item.getArticleId()));

            if (item.getQuantityKg() != null && article.getStockKg().compareTo(item.getQuantityKg()) < 0) {
                throw new IllegalArgumentException("Insufficient stock in kg for article: " + article.getName());
            }

            if (item.getQuantity() != null && article.getStockUnit().compareTo(item.getQuantity()) < 0) {
                throw new IllegalArgumentException("Insufficient stock in units for article: " + article.getName());
            }
        }
    }

    private BigDecimal updateStockAndComputeTotal(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : items) {
            Article article = articleRepository.findById(item.getArticleId())
                    .orElseThrow(() -> new IllegalArgumentException("Article not found: " + item.getArticleId()));

            if (item.getQuantityKg() != null) {
                article.setStockKg(article.getStockKg().subtract(item.getQuantityKg()));
            }

            if (item.getQuantity() != null) {
                article.setStockUnit(article.getStockUnit().subtract(item.getQuantity()));
            }

            articleRepository.save(article);

            BigDecimal qty = item.getQuantityKg() != null ? item.getQuantityKg() : item.getQuantity();
            total = total.add(item.getPrice().multiply(qty));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
    }
}
