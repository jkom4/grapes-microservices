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

    /**
     * Converts a Client entity to ClientDTO.
     * Sensitive fields like password and nationalRegistryNumber are not mapped.
     *
     * @param client The client entity to convert
     * @return The corresponding DTO
     */
    public static ClientDTO toDto(Client client) {
        if (client == null) return null;

        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setPhoneNumber(client.getPhoneNumber());
        dto.setBirthDate(client.getBirthDate());
        dto.setAddress(client.getAddress());
        dto.setGender(client.getGender());
        dto.setMaritalStatus(client.getMaritalStatus());
        dto.setAverageMonthlySalary(client.getAverageMonthlySalary());

        return dto;
    }
}