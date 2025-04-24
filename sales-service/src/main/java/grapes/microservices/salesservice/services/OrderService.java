package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.config.SalesDataMessage;
import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.DeliveryMessage;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import grapes.microservices.salesservice.repositories.OrderItemRepository;
import grapes.microservices.salesservice.repositories.OrderRepository;
import grapes.microservices.salesservice.utils.InvoiceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ArticleRepository articleRepository;
    private final RabbitTemplate rabbitTemplate;

    //  SEND a message to RabbitMQ for delivery
    public void sendOrderToDeliveryQueue(Integer orderId, String address, String phoneNumber, String customerName) {
        DeliveryMessage message = new DeliveryMessage(orderId, address, phoneNumber, customerName);
        rabbitTemplate.convertAndSend("order-paid-queue", message);
        System.out.println(" Message sent to RabbitMQ: " + message);
    }

    public Order createTemporaryOrder(Integer userId) {
        Order order = Order.builder()
                .userId(userId)
                .isFinished(false)
                .isPaid(false)
                .totalPrice(null)
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }


    //  Finalize payment
    @CacheEvict(value = "articles", allEntries = true)
    public void finalizePaymentAndClearCart(Integer orderId, String address, String phoneNumber, String customerName) throws FileNotFoundException {
        Order order = getOrderById(orderId);
        List<OrderItem> items = getValidOrderItems(orderId);

        validateStockForItems(items);
        BigDecimal total = updateStockAndComputeTotal(items);
        order.setTotalPrice(total);

        String pdfPath = InvoiceGenerator.generateInvoice(order, customerName, address, phoneNumber, items, articleRepository);
        order.setFacturePath(pdfPath);
        order.setPaid(true);

        orderRepository.save(order);
        sendToDataMining(order, items);

        //  Send to RabbitMQ to create the delivery
        sendOrderToDeliveryQueue(order.getId(), address, phoneNumber, customerName);
    }

    //  Retrieve all valid items from an order
    private List<OrderItem> getValidOrderItems(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty or already processed.");
        }
        return items;
    }

    //  Validate stock for each item
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

    //  Update stock and calculate total
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

    //  Retrieve an order
    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
    }

    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public OrderDTO mapOrderItemToDTO(OrderItem item) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderItemId(item.getId());
        dto.setQuantity(item.getQuantityKg() != null ? item.getQuantityKg() : item.getQuantity());
        dto.setTripId(item.getOrderId()); // ou autre selon ta logique
        dto.setScanned(Boolean.TRUE.equals(item.getScanned()));
        dto.setProductDescription(
                articleRepository.findById(item.getArticleId())
                        .map(Article::getName)
                        .orElse("Unknown")
        );
        return dto;
    }

    public void sendToDataMining(Order order, List<OrderItem> items) {
        List<SalesDataMessage.ItemInfo> itemInfos = items.stream()
                .map(item -> new SalesDataMessage.ItemInfo(
                        item.getArticleId(),
                        item.getPrice(),
                        item.getQuantityKg() != null ? item.getQuantityKg() : item.getQuantity()
                ))
                .collect(Collectors.toList());

        SalesDataMessage message = new SalesDataMessage(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemInfos
        );

        rabbitTemplate.convertAndSend("sales-data-queue", message);
        System.out.println("[Sales-Service] Sent sales data to DataMining: " + message);
    }



}
