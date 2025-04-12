package grapes.microservices.authservice.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Address {

    @NotBlank(message = "Street is required")
    @Size(max = 100, message = "Street must be at most 100 characters")
    private String street;

    @NotBlank(message = "Number is required")
    @Size(max = 4, message = "Number must be at most 10 characters")
    private String number;

    @NotBlank(message = "Province is required")
    @Size(max = 50, message = "Province must be at most 50 characters")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{4,10}", message = "Postal code must be between 4 and 10 digits")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 56, message = "Country name must be at most 56 characters")
    private String country;
}
