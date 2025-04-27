package grapes.microservices.authservice.dto;

public record AuthEvent(
        String eventId,
        String eventType,
        String eventTimestamp,
        String sourceSystem,
        String version,
        EventPayload payload
) {}