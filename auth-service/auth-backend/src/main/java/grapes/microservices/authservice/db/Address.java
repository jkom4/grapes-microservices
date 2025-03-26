package grapes.microservices.authservice.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "addresses")
public class Address {

    @Id
    private String id;

    private String street;
    private String postalCode;
    private String country;
    private String countryCode;
    private String number;
    private String province;

    // Getters and Setters
}
