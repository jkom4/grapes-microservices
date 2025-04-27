package grapes.microservices.authservice.models;

import lombok.Getter;

@Getter
public enum Role {
    USER("USER"), DELIVERY("DELIVERY"), ADMIN("ADMIN");

    private final String role;

    Role(String role) {
        this.role = role;
    }
}
