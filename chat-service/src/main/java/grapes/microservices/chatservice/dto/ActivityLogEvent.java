package grapes.microservices.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogEvent {
    private String eventId;
    private String eventType;
    private String eventTimestamp;
    private String sourceSystem;
    private String version;
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String sourceTransactionId;
        private String clientId;
        private String productId;
        private String serviceId;
        private String transactionTimestamp;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String currency;
        private String paymentMethod;
        private String paymentStatus;
        private String deliveryStatus;
        private Integer deliveryTimeDays;
    }
}
