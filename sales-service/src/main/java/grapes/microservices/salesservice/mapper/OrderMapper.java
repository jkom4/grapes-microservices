package grapes.microservices.salesservice.mapper;

import grapes.microservices.salesservice.dto.OrderSummaryDTO;
import grapes.microservices.salesservice.models.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderSummaryDTO toSummaryDTO(Order order);
}


