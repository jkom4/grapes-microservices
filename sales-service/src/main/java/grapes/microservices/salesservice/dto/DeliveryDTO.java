package grapes.microservices.salesservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryDTO {
    private Integer id;
    private Integer orderId;
    private LocalDateTime deliveryDate;
    private String statusLabel;
}
