package challengers.momochat.infrastructure.event.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DltProcessor { // dlt topic으로 보내는 서비스

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DltProcessor(@Qualifier("kafkaDltTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToDlt(Object event) {
        String topicName = "momo-chat-dlt-topic";

        try{
            kafkaTemplate.send(topicName, event).get();
            log.info("DLQ 전송 성공: {}", event);
        } catch (Exception e) {
            log.error("DLQ 전송 실패: {}", event, e);
        }

    }
}
