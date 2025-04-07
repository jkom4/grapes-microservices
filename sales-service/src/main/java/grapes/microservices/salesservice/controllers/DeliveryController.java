package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.DeliveryDTO;
import grapes.microservices.salesservice.services.DeliveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cll/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/pending")
    public List<DeliveryDTO> getPendingDeliveries() {
        return deliveryService.getPendingDeliveries();
    }
}
