package grapes.microservices.paymentbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity representing a client in the payment system.
 * Maps to the "client" table in the database.
 */
@Data
@Entity
@Table(name = "client")
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @Column(name = "client_id")
    private Long id;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "average_monthly_salary")
    private BigDecimal averageMonthlySalary;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "national_registry_number")
    private String nationalRegistryNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Account> accounts;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Card> cards;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}