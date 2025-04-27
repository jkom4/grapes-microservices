package grapes.microservices.authservice.dto;

import java.util.Date;
import lombok.Data;
import grapes.microservices.authservice.models.Gender;


@Data
public class EIDCardInfo {
    private String firstName;
    private String lastName;
    private String nationalId;
    private Date birthDate;
    private Gender gender;
}