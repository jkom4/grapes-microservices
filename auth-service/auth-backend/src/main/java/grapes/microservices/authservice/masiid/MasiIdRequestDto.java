package grapes.microservices.authservice.masiid;

import lombok.Data;

@Data
public class MasiIdRequestDto {
    private String clientName;
    private String password;
    private String nationalRegistryNumber;
    private String birthDate;
    private String gender;
    private String email;
    private String cardNumber;
    private String pinCode;
}
