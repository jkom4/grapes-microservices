package grapes.microservices.authservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    private String id;

    private String userId;
    private String token;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String ipAddress;
}
