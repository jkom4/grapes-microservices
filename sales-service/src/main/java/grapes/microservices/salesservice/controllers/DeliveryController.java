package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.DeliveryDTO;
import grapes.microservices.salesservice.services.DeliveryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cll/deliveries")
@AllArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    //  Get all pending deliveries (for delivery drivers)
// 1. EXISTING: Get all pending deliveries (for delivery drivers)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDeliveries() {
        try {
            List<DeliveryDTO> pendingDeliveries = deliveryService.getPendingDeliveries();
            return ResponseEntity.ok(pendingDeliveries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }


    // Track the delivery status by order ID (for customers)
    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getDeliveryStatus(@PathVariable Integer orderId) {
        try {
            String status = deliveryService.getDeliveryStatusByOrderId(orderId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    //  Update the delivery status (Pending ➔ In Progress ➔ Delivered)
    @PatchMapping("/update-status/{orderId}")
    public ResponseEntity<String> updateDeliveryStatus(@PathVariable Integer orderId, @RequestParam String newStatus) {
        try {
            deliveryService.updateDeliveryStatus(orderId, newStatus);
            return ResponseEntity.ok("Delivery status updated successfully to: " + newStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}
