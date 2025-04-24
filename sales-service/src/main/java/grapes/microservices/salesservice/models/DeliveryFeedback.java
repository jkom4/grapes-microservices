package grapes.microservices.salesservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryFeedback {
    private Integer orderId;
    private String comment;
    private boolean doorstep;
    private byte[] signature;
}