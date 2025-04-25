package grapes.microservices.salesservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesDataMessage {
    private Integer orderId;
    private String  userId;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<ItemInfo> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemInfo {
        private Integer articleId;
        private BigDecimal price;
        private BigDecimal quantity;
    }
}
