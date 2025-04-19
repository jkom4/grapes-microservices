package grapes.microservices.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private Integer orderId;
    private String address;
    private String phoneNumber;
    private String customerName;
    private String country;
    private String postalCode;
}
