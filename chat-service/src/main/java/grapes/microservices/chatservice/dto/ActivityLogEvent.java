package grapes.microservices.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogEvent {
    private UUID eventId;
    private String eventType;
    private Instant eventTimestamp;
    private String sourceSystem;
    private String version;
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String usage_log_id_source;
        private String client_id;
        private String product_id;
        private String service_id;
        private Instant usage_timestamp;
        private Object request_details;
        private String status;
        private Long duration_ms;
    }
}
