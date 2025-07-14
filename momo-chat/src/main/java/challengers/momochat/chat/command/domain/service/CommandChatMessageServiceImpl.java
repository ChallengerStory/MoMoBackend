package challengers.momochat.chat.command.domain.service;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import challengers.momochat.chat.command.application.service.CommandChatMessageService;
import challengers.momochat.chat.command.domain.aggregate.entity.ChatMessage;
import challengers.momochat.chat.command.domain.repository.CommandChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class CommandChatMessageServiceImpl implements CommandChatMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate; // simple broker
    private final CommandChatMessageRepository commandChatMessageRepository; // chat message save
    private final KafkaTemplate<String, Object> kafkaTemplate; // send TO momo-chat-topic
    private final RedisTemplate<String, Object> redisTemplate; // redis idempotencyKey
    @Value("${spring.kafka.topic.chat}")
    private String topicName;

    @Autowired
    public CommandChatMessageServiceImpl(
            SimpMessagingTemplate simpMessagingTemplate,
            CommandChatMessageRepository commandChatMessageRepository,
            @Qualifier("kafkaChatTemplate") KafkaTemplate<String, Object> kafkaTemplate, // 동일한 여러개의 kafka template bean name 구분
            RedisTemplate<String, Object> redisTemplate
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.commandChatMessageRepository = commandChatMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }


    // 1. stomp. simple broker
    @Transactional
    @Override
    public void sendMessageByStomp(ChatMessageDTO chatMessageDTO) {

        // 엔티티로 변환
        ChatMessage chatMessage = ChatMessage.fromDTO(chatMessageDTO);

        commandChatMessageRepository.save(chatMessage);

        simpMessagingTemplate.convertAndSend("/topic/message/"
                + chatMessageDTO.getChatRoomId(), chatMessageDTO);
    }

    // 2. kafka
    @Transactional
    @Override
    public void sendMessageByKafka(ChatMessageDTO chatMessageDTO){

        ChatMessage chatMessage = ChatMessage.fromDTO(chatMessageDTO);

        commandChatMessageRepository.save(chatMessage);

        kafkaTemplate.send(topicName, chatMessageDTO.getChatRoomId(), chatMessageDTO);
    }

    // 3. kafka + idempotency key
    @Transactional
    @Override
    public void sendMessageByKafkaIdempotencyKey(ChatMessageIdempotencyKeyDTO dto) {

        String idempotencyKey = dto.getIdempotencyKey();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("요청에 유효하지 않은 idempotencyKey가 포함되어 있습니다. 요청 거부됨.");
            return;
        }

        Boolean isNewRequest = redisTemplate
                .opsForValue()
                .setIfAbsent(idempotencyKey, "1", Duration.ofMinutes(3));

        if (Boolean.FALSE.equals(isNewRequest)) {
            log.info("중복 요청: 이미 처리된 idempotencyKey = {}", idempotencyKey);
            return;
        }

        try {
            ChatMessage chatMessage = ChatMessage.fromDTO(dto);

            // DB 저장
            commandChatMessageRepository.save(chatMessage);

            // Kafka 전송
            kafkaTemplate.send(topicName, dto.getChatRoomId(), dto);

            log.info("Kafka 전송 및 DB 저장 완료: {}", dto);
        } catch (Exception e) {
            // 실패하면 Redis에 저장된 멱등키 제거 (재시도 가능하도록)
            redisTemplate.delete(idempotencyKey);
            log.error("메시지 처리 중 오류 발생. 멱등키 제거 및 롤백", e);
            throw e; // 트랜잭션 롤백
        }
    }


    // 4. kafka + idempotency key + outbox
    @Transactional
    @Override
    public void sendMessageByKafkaIdempotencyKeyOutbox(ChatMessageDTO chatMessageDTO) {

        // 1. 멱등성 체크

        // 2. 전송 메시지 db 저장

        // 3. 도메인 이벤트(채팅 전달됨)

        // 4. 이벤트 직렬화

        // 5. outbox 엔티티 생성 + 멱등키를 통한 중복 방지

        // 6. outbox 등록

        // 7. 실패시 레디스 키 삭제 + 트랜잭션 롤백

    }

}
