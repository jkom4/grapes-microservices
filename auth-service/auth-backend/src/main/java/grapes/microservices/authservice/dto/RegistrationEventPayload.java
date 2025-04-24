package grapes.microservices.authservice.dto;

import grapes.microservices.authservice.models.Address;
import grapes.microservices.authservice.models.Gender;

import java.util.Date;

public record RegistrationEventPayload (
        String client_id,
        String registration_id,
        String timestamp,
        String email,
        String name,
        String firstName,
        Gender gender,
        Date birthDate,
        String nationalId,
        Address address,
        String addressAsString
) implements EventPayload {
}
