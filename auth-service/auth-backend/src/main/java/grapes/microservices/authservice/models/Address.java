package grapes.microservices.authservice.models;

import lombok.Data;

@Data
public class Address {
    private String street;
    private String postalCode;
    private String country;
    private String countryCode;
    private String number;
    private String province;
}
