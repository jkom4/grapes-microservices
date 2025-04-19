package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.services.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cll/orders")
@RequiredArgsConstructor
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;

    @GetMapping("/{tripId}")
    public ResponseEntity<List<OrderDTO>> getOrdersForTrip(@PathVariable Integer tripId) {
        List<OrderDTO> orders = orderDeliveryService.getOrdersForTrip(tripId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/scan/{orderItemId}")
    public ResponseEntity<String> markOrderItemAsScanned(@PathVariable Integer orderItemId) {
        orderDeliveryService.markOrderItemAsScanned(orderItemId);
        return ResponseEntity.ok("OrderItem marked as scanned successfully.");
    }

    @PatchMapping("/scan-all/{tripId}")
    public ResponseEntity<String> markAllOrderItemsAsScanned(@PathVariable Integer tripId) {
        orderDeliveryService.markAllOrderItemsAsScannedForTrip(tripId);
        return ResponseEntity.ok("All order items for trip " + tripId + " marked as scanned successfully.");
    }



}
