package grapes.microservices.salesservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryMessage implements Serializable {
    private Integer orderId;
    private String address;
    private String phoneNumber;
    private String customerName;
}