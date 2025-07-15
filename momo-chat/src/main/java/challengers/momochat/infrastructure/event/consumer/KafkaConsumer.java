package challengers.momochat.infrastructure.event.consumer;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    // 1. kafka + stomp
//    @KafkaListener(
//            topics = "${spring.kafka.topic.chat}",
//            groupId = "${spring.kafka.consumer.group-id}",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void consumeChatMessage(ChatMessageDTO chatMessageDTO, Acknowledgment ack) {
//        try {
//            log.info("Kafka 메시지 수신: chatRoomId={}, memberId={}, message={}",
//                    chatMessageDTO.getChatRoomId(),
//                    chatMessageDTO.getMemberId(),
//                    chatMessageDTO.getMessage());
//
//            // TODO: Stomp 또는 다른 처리 로직 수행
//
//            // 수동 커밋
//            ack.acknowledge();
//            log.info("Kafka 메시지 커밋 완료");
//        } catch (Exception e) {
//            log.error("Kafka 메시지 처리 중 오류 발생", e);
//            // 예외 발생 시 ack.acknowledge() 하지 않음 → 재처리 가능
//        }
//    }

    // 2. kafka + stomp + idempotencyKey
    @KafkaListener(
            topics = "${spring.kafka.topic.chat}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeChatMessage(ChatMessageIdempotencyKeyDTO dto, Acknowledgment ack) {
        try {
            log.info("Kafka 메시지 수신: chatRoomId={}, memberId={}, message={}",
                    dto.getChatRoomId(),
                    dto.getMemberId(),
                    dto.getMessage());

            // TODO: Stomp 또는 다른 처리 로직 수행

            // 수동 커밋
            ack.acknowledge();
            log.info("Kafka 메시지 커밋 완료");
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생", e);
            // 예외 발생 시 ack.acknowledge() 하지 않음 → 재처리 가능
        }
    }
}

