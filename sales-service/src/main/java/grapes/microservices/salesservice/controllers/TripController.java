package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.dto.TripDTO;
import grapes.microservices.salesservice.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/cll/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<TripDTO>> getTripsByUserId(@PathVariable String userId) {
        List<TripDTO> trips = tripService.getTripsByDeliveryMan(userId);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{tripId}/orders")
    public ResponseEntity<List<OrderDTO>> getOrdersForTrip(@PathVariable Integer tripId) {
        List<OrderDTO> orders = tripService.getOrdersForTrip(tripId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{orderItemId}/scan")
    public ResponseEntity<Map<String, Integer>> scanOrderItem(@PathVariable Integer orderItemId) {
        tripService.updateScanStatus(orderItemId);

        Map<String, Integer> response = new HashMap<>();
        response.put("scannedOrderItemId", orderItemId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tripId}/finish")
    public ResponseEntity<?> finishTrip(@PathVariable Integer tripId) {
        try {
            tripService.finishTrip(tripId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }




}

