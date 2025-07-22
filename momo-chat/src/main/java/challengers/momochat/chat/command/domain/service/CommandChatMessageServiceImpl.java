package challengers.momochat.chat.command.domain.service;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import challengers.momochat.chat.command.application.service.CommandChatMessageService;
import challengers.momochat.chat.command.domain.aggregate.entity.ChatMessage;
import challengers.momochat.chat.command.domain.repository.CommandChatMessageRepository;
import challengers.momochat.infrastructure.outbox.dto.OutboxDTO;
import challengers.momochat.infrastructure.outbox.entity.Outbox;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;
import challengers.momochat.infrastructure.outbox.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
@Slf4j
public class CommandChatMessageServiceImpl implements CommandChatMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate; // simple broker
    private final CommandChatMessageRepository commandChatMessageRepository; // chat message save
    private final KafkaTemplate<String, Object> kafkaTemplate; // send TO momo-chat-topic
    private final RedisTemplate<String, Object> redisTemplate; // redis idempotencyKey
    private final ObjectMapper objectMapper; // 이벤트 직렬화
    private final OutboxService outboxService;
    @Value("${spring.kafka.topic.chat}")
    private String topicName;

    @Autowired
    public CommandChatMessageServiceImpl(
            SimpMessagingTemplate simpMessagingTemplate,
            CommandChatMessageRepository commandChatMessageRepository,
            @Qualifier("kafkaChatTemplate") KafkaTemplate<String, Object> kafkaTemplate, // 동일한 여러개의 kafka template bean name 구분
            RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, OutboxService outboxService
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.commandChatMessageRepository = commandChatMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
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
    public void sendMessageByKafkaIdempotencyKeyOutbox(ChatMessageIdempotencyKeyDTO dto) {
        // 1. 멱등성 체크
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

        try{
            // 2. 전송 메시지 db 저장
            ChatMessage savedMessage = commandChatMessageRepository.save(ChatMessage.fromDTO(dto));

            // 2. 이벤트 직렬화
            String payload = objectMapper.writeValueAsString(dto);

            // 3. outbox 생성
            OutboxDTO outboxDTO = OutboxDTO.builder()
                    .aggregateId(savedMessage.getChatMessageId().toHexString()) // ObjectId -> String
                    .payload(payload)
                    .idempotencyKey(idempotencyKey)
                    .build();

            // 4. outbox service 호출
            outboxService.createOutbox(outboxDTO);

        } catch (JsonProcessingException e) {
            redisTemplate.delete(idempotencyKey);
            log.error("직렬화(ObjectMapper) 오류 발생. 멱등키 제거 및 롤백", e);
            throw new RuntimeException("직렬화 실패", e);

        } catch (Exception e) {
            redisTemplate.delete(idempotencyKey);
            log.error("트랜잭션 또는 기타 오류 발생. 멱등키 제거 및 롤백", e);
            throw new RuntimeException("처리 중 오류 발생", e);
        }
    }

    // 메시지 브로드캐스트
    @Override
    public void sendToStomp(ChatMessageIdempotencyKeyDTO dto) {
        log.info("/topic/message/" + dto.getChatRoomId());
        simpMessagingTemplate.convertAndSend("/topic/message/" + dto.getChatRoomId(), dto);
    }

}
