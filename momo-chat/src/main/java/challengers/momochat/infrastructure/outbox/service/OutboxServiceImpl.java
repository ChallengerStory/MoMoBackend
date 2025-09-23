package challengers.momochat.infrastructure.outbox.service;

import challengers.momochat.infrastructure.outbox.dto.OutboxDTO;
import challengers.momochat.infrastructure.outbox.entity.Outbox;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;
import challengers.momochat.infrastructure.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository outboxRepository;

    @Override
    public void createOutbox(OutboxDTO outboxDTO) {outboxRepository.save(Outbox.toEntity(outboxDTO));}

    // 처리되지 않은 OUTBOX들을 STATUS로 찾아오는 메서드
    @Override
    public List<Outbox> findByStatus(List<OutboxStatus> statusList){
        return outboxRepository.findByStatusIn(statusList);
    }

    @Override
    public void changeStatus(Outbox event) {
        Optional<Outbox> outboxOptional = outboxRepository.findById(event.getOutboxId());
        if (outboxOptional.isPresent()) {
            Outbox updateOutbox = outboxOptional.get();
            updateOutbox.setStatus(event.getStatus());
            updateOutbox.setRetryCount(event.getRetryCount());
            outboxRepository.save(updateOutbox);
        } else {
            log.info("해당 event의 Outbox가 존재하지 않습니다.");
        }
    }

    @Override
    public void changeStatusAndRetryCount(Outbox event) {
        Optional<Outbox> outboxOptional = outboxRepository.findById(event.getOutboxId());

        if (outboxOptional.isPresent()) {
            Outbox updateOutbox = outboxOptional.get();
            updateOutbox.setStatus(OutboxStatus.RETRYING);
            updateOutbox.setRetryCount(0);
            outboxRepository.save(updateOutbox);
        } else {
            log.info("해당 event의 Outbox가 존재하지 않습니다.");
        }
    }
}
