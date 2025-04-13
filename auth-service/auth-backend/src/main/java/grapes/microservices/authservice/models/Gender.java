package grapes.microservices.authservice.models;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("Male"), FEMALE("Female"), OTHER("Other"), UNKNOWN("Unknown");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

}
