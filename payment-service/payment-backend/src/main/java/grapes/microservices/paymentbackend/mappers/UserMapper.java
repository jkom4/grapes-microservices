package grapes.microservices.paymentbackend.mappers;

import grapes.microservices.paymentbackend.dto.ClientDTO;
import grapes.microservices.paymentbackend.models.Client;
import org.springframework.stereotype.Component;

/**
 * Mapper for User/Client entity conversion.
 * Provides methods to convert between Client entities and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Converts a ClientDTO to a Client entity.
     *
     * @param dto The client DTO to convert
     * @return The Client entity
     */
    public static Client toEntity(ClientDTO dto) {
        if (dto == null) return null;

        Client client = new Client();
        client.setId(dto.getId());
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setPhoneNumber(dto.getPhoneNumber());
        client.setBirthDate(dto.getBirthDate());
        client.setAddress(dto.getAddress());
        client.setGender(dto.getGender());
        client.setMaritalStatus(dto.getMaritalStatus());
        client.setAverageMonthlySalary(dto.getAverageMonthlySalary());
        client.setNationalRegistryNumber(dto.getNationalRegistryNumber());

        return client;
    }


}