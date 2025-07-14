package challengers.momochat.chat.command.application.service;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import org.springframework.transaction.annotation.Transactional;

public interface CommandChatMessageService {
    // 1. stomp. simple broker
    @Transactional
    void sendMessageByStomp(ChatMessageDTO chatMessageDTO);

    // 2. kafka
    @Transactional
    void sendMessageByKafka(ChatMessageDTO chatMessageDTO);

    // 3. kafka with idempotency key
    void sendMessageByKafkaIdempotencyKey(ChatMessageIdempotencyKeyDTO chatMessageIdempotencyKeyDTO);

    // 4. kafka with idempotency key + outbox
    @Transactional
    void sendMessageByKafkaIdempotencyKeyOutbox(ChatMessageDTO chatMessageDTO);
}