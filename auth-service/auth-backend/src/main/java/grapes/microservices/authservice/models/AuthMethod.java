package grapes.microservices.authservice.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Authentication methods available in the service
 */
@Getter
@RequiredArgsConstructor
public enum AuthMethod {
    EMAIL("EMAIL"),
    SMS("SMS");

    private final String name;
}
