package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.OrderDTO;
import grapes.microservices.salesservice.dto.TripDTO;
import grapes.microservices.salesservice.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cll/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<TripDTO>> getTripsByUserId(@PathVariable Integer userId) {
        List<TripDTO> trips = tripService.getTripsByDeliveryMan(userId);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{tripId}/orders")
    public ResponseEntity<List<OrderDTO>> getOrdersForTrip(@PathVariable Integer tripId) {
        List<OrderDTO> orders = tripService.getOrdersForTrip(tripId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{orderItemId}/scan")
    public ResponseEntity<Void> scanOrderItem(@PathVariable Integer orderItemId) {
        tripService.updateScanStatus(orderItemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tripId}/finish")
    public ResponseEntity<Void> finishTrip(@PathVariable Integer tripId) {
        tripService.finishTrip(tripId);
        return ResponseEntity.noContent().build();
    }

}

