package grapes.microservices.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PinRequest {

    private String currentPin;
    private String updatedPin;
}
