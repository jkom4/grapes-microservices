package grapes.microservices.salesservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryDTO {
    private Integer id;
    private Integer code;
    private Integer userId;
    private String facturePath;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private boolean isPaid;
    private boolean isFinished;
}
