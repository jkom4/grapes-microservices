package grapes.microservices.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Integer orderItemId;
    private String productDescription;
    private BigDecimal quantity;
    private Integer tripId;
    private boolean isScanned;
}
