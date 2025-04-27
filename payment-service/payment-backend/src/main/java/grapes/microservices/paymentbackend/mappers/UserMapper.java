package grapes.microservices.paymentbackend.mappers;

import grapes.microservices.paymentbackend.dto.ClientDTO;
import grapes.microservices.paymentbackend.models.Client;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for User/Client entity conversion.
 * Uses MapStruct to automatically generate mapping code between Client entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts a ClientDTO to a Client entity.
     *
     * @param dto The client DTO to convert
     * @return The Client entity
     */
    Client toEntity(ClientDTO dto);

}