package grapes.microservices.authservice.dto;

public record AuthEventPayload(
        String client_id,
        String auth_attempt_id,
        String timestamp,
        String authentication_method,
        String status,
        String source_ip,
        String user_agent,
        String application_type,
        String failure_reason
) {
}
