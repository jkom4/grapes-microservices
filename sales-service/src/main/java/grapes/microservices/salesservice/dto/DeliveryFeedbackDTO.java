package grapes.microservices.salesservice.dto;

import lombok.Data;

@Data
public class DeliveryFeedbackDTO {
    private Integer orderId;
    private String comment;
    private boolean doorstep;
    private byte[] signature;
    private Integer deliveryStatusId = 2;
}