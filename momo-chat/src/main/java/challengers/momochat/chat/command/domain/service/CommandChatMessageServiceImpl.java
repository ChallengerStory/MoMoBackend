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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CommandChatMessageServiceImpl implements CommandChatMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate; // simple broker
    private final CommandChatMessageRepository commandChatMessageRepository; // chat message save
    private final KafkaTemplate<String, Object> kafkaTemplate; // send TO momo-chat-topic
    @Value("${spring.kafka.topic.chat}")
    private String topicName;

    @Autowired
    public CommandChatMessageServiceImpl(
            SimpMessagingTemplate simpMessagingTemplate,
            CommandChatMessageRepository commandChatMessageRepository,
            @Qualifier("kafkaChatTemplate") KafkaTemplate<String, Object> kafkaTemplate // 동일한 여러개의 kafka template bean name 구분
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.commandChatMessageRepository = commandChatMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
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

    // 3. kafka with idempotency key
    @Transactional
    @Override
    public void sendMessageByKafkaIdempotencyKey(ChatMessageIdempotencyKeyDTO chatMessageIdempotencyKeyDTO) {

    }


    // 4. kafka with idempotency key + outbox
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
