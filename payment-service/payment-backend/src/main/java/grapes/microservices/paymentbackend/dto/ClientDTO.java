package grapes.microservices.paymentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for client information transfer between application layers.
 * Contains personal and financial data needed for account management and authentication.
 * Some fields are used only during account creation and not returned in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String address;
    private String gender;
    private String maritalStatus;
    private BigDecimal averageMonthlySalary;

    // Security-sensitive fields used only for account creation - not returned in API responses
    private String password;
    private String nationalRegistryNumber;
}