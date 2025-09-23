package challengers.momochat.infrastructure.outbox.service;

import challengers.momochat.infrastructure.outbox.dto.OutboxDTO;
import challengers.momochat.infrastructure.outbox.entity.Outbox;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;

import java.util.List;

public interface OutboxService {

    void createOutbox(OutboxDTO outboxDTO);

    // 처리되지 않은 OUTBOX들을 STATUS로 찾아오는 메서드
    List<Outbox> findByStatus(List<OutboxStatus> statusList);

    void changeStatus(Outbox event);

    void changeStatusAndRetryCount(Outbox event);
}
