package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for messages received on the auth registration queue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegistrationMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String email;
    private String name;
    private String firstName;
    private String gender;
    private LocalDate birth_date;
    private String national_id;
    private String address;
}