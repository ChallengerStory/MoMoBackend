package challengers.momochat.infrastructure.outbox.entity;

import challengers.momochat.infrastructure.outbox.dto.OutboxDTO;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "outbox")
public class Outbox {//implements Persistable<ObjectId> {

    // Persistable?? -> isNew 사용 가능케 하는것
    @Id
    @Builder.Default // builder 패턴 사용시 default 값 무시되는 오류 제거
    private ObjectId outboxId = ObjectId.get();
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String aggregateId;
//    private EventType eventType;
    private String payload;
    private OutboxStatus status;
    @Builder.Default
    private int retryCount = 0;
    @Builder.Default
    private int dlqRetryCount = 0;
    @Indexed(unique = true) // 인덱스 생성 + UNIQUE
    private String idempotencyKey;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

//    @Override
//    public ObjectId getId() {
//        return outboxId;
//    }
//
//    @Override
//    @Transient
//    public boolean isNew() {
//        return createdAt == null;
//    }

    public static Outbox toEntity(OutboxDTO dto){
        return Outbox.builder()
                .aggregateId(dto.getAggregateId())
                .payload(dto.getPayload())
                .status(OutboxStatus.PENDING)
                .idempotencyKey(dto.getIdempotencyKey())
                .build();
    }

}