package grapes.microservices.authservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AmountRequest {

    private int amount;
}
