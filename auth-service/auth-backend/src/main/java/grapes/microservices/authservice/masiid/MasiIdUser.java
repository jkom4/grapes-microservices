package grapes.microservices.authservice.masiid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "masiid_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasiIdUser {
    private String clientName;
    private String password;
    private String nationalRegistryNumber;
    private String birthDate;
    private int ageAtRequest;
    private String gender;
    private LocalDateTime requestDateTime;
    private String email;
    private String receivedDigest;
    private LocalDateTime receivedDateTime;
    private boolean isLate;
    private String cardNumber;
    private String recalculatedDigest;
    private String decision;
    private LocalDateTime decisionDateTime;
}
