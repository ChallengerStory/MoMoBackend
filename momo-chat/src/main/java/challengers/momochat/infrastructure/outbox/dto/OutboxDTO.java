package challengers.momochat.infrastructure.outbox.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxDTO {
    private String aggregateId;
    //    private EventType eventType;
    private String payload;
    private String idempotencyKey;
}
