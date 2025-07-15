package challengers.momochat.infrastructure.event.processor;

import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import challengers.momochat.infrastructure.outbox.entity.Outbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OutboxProcessor { // outbox kafka로 보내는 것.

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topic.chat}")
    private String topicName;

    public OutboxProcessor(@Qualifier("kafkaChatTemplate") KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * outbox 이벤트를 kafka로 동기식 전송
     * 재시도 정책을 적용하여 일시적 오류 발생시에 최대 3회 재시도
     * */
    @Retryable(
            value = { TransientDataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true)
    )
    public void process(Outbox event) {

        try {
            String payloadJson = event.getPayload();
            ChatMessageIdempotencyKeyDTO dto = objectMapper.readValue(payloadJson, ChatMessageIdempotencyKeyDTO.class);

            // -------------------------------------------------------------------------------------
            // "fail"이 포함되어 있으면 테스트용 예외 발생
            if (payloadJson != null && payloadJson.contains("fail")) {
                log.warn("테스트용 예외 발생: payload에 'fail' 포함됨 - outboxId={}", event.getOutboxId());
                throw new TransientDataAccessResourceException("테스트: 재시도 대상 예외");
            }
            // -------------------------------------------------------------------------------------

            SendResult<String, Object> result = kafkaTemplate.send(topicName, event.getAggregateId(), dto).get();
            log.info("Kafka 전송 성공 - outboxId={}, partition={}, offset={}",
                    event.getOutboxId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.warn("Outbox 이벤트 전송 실패: {}", event.getOutboxId(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * @Recover는 ?
     * @Retryable로 지정된 함수가 모든 재시도에도 실패했을 때 호출되는 메서드
     * 시그니처는 예외 + 동일한 파라미터들
     * 최종 fallback 로직 또는 로깅 전용으로 자주 사용
     */
    @Recover
    public void recover(Exception e, Outbox event) {
        log.error("Kafka 전송 3회 실패 → Recover 진입: {}", event.getOutboxId(), e);
        throw new RuntimeException(e); // DLQ는 Publisher 쪽에서 처리함
    }
}
