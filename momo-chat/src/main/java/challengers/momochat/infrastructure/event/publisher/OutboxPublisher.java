package challengers.momochat.infrastructure.event.publisher;

import challengers.momochat.infrastructure.event.processor.DltProcessor;
import challengers.momochat.infrastructure.event.processor.OutboxProcessor;
import challengers.momochat.infrastructure.outbox.entity.Outbox;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;
import challengers.momochat.infrastructure.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxService outboxService;
    private final OutboxProcessor outboxProcessor;
    private final DltProcessor dltProcessor;

    @Scheduled(fixedDelay = 1000)
    public void publishOutbox() {

        int maxRetry = 3;

        List<Outbox> pendingOutbox = outboxService.findByStatus(
                Arrays.asList(OutboxStatus.PENDING, OutboxStatus.RETRYING)
        );

        for (Outbox event : pendingOutbox) {
            try {
                // 중복 방지: 상태를 PROCESSING으로 먼저 변경
                event.setStatus(OutboxStatus.PROCESSING);
                outboxService.changeStatus(event);
                log.info("publisher 처리중: {}", event);

                // Kafka로 전송
                outboxProcessor.process(event);
                log.info("publisher 처리 완료: {}", event);

                // 처리 성공 → SUCCESS로 상태 변경
                event.setStatus(OutboxStatus.SUCCESS);
                outboxService.changeStatus(event);

            } catch (Exception e) {
                log.error("Outbox 이벤트 처리 중 예외 발생: {}", event, e);

                // 재시도 카운트 증가
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= maxRetry) {
                    // 재시도 초과 시 DLQ(DLT)로 전송
                    event.setStatus(OutboxStatus.DEAD);
                    dltProcessor.sendToDlt(event);
                } else {
                    event.setStatus(OutboxStatus.RETRYING);
                }

                outboxService.changeStatus(event);
            }
        }
    }
}
