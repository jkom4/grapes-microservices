package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.dto.OrderSummaryDTO;
import grapes.microservices.salesservice.mapper.OrderMapper;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/cll/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Integer id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch order: " + e.getMessage());
        }
    }


    @GetMapping("/history/{userId}")
    public ResponseEntity<List<OrderSummaryDTO>> getOrderHistory(@PathVariable Integer userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<OrderSummaryDTO> dtos = orders.stream()
                .map(orderMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }



    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderDTO>> getOrderItems(@PathVariable Integer orderId) {
        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);

        List<OrderDTO> dtos = items.stream()
                .map(orderService::mapOrderItemToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


}
