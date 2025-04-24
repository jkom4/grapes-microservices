package grapes.microservices.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripDTO {
    private Integer id;
    private String name;
    private String distance;
    private String address;
    private boolean isFinished;
}