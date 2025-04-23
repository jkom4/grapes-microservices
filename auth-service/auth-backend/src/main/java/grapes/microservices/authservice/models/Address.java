package grapes.microservices.authservice.models;

import grapes.microservices.authservice.utils.validators.WithoutPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Address {

    @NotBlank(message = "Street is required", groups = WithoutPassword.class)
    @Size(max = 100, message = "Street must be at most 100 characters", groups = WithoutPassword.class)
    private String street;

    @NotBlank(message = "Number is required", groups = WithoutPassword.class)
    @Size(max = 4, message = "Number must be at most 10 characters", groups = WithoutPassword.class)
    private String number;

    @NotBlank(message = "City is required", groups = WithoutPassword.class)
    @Size(max = 50, message = "City must be at most 50 characters", groups = WithoutPassword.class)
    private String city;

    @NotBlank(message = "Postal code is required", groups = WithoutPassword.class)
    @Pattern(regexp = "\\d{4,10}", message = "Postal code must be between 4 and 10 digits", groups = WithoutPassword.class)
    private String postalCode;

    @NotBlank(message = "Country is required", groups = WithoutPassword.class)
    @Size(max = 56, message = "Country name must be at most 56 characters", groups = WithoutPassword.class)
    private String country;
}
