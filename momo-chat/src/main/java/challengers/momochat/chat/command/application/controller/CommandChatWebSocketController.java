package challengers.momochat.chat.command.application.controller;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatMessageIdempotencyKeyDTO;
import challengers.momochat.chat.command.application.service.CommandChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CommandChatWebSocketController { // web socket 엔드포인트 구성

    private final CommandChatMessageService commandChatMessageService;

    // 1. stomp
    @MessageMapping("stomp/{chatRoomId}")
    public void sendMessageByStomp(
            @DestinationVariable String chatRoomId,
            ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor // ws 연결된 사용자 세션정보(사용자 정보)
    ) {
        log.info("Sending message: {}", chatMessageDTO);

        commandChatMessageService.sendMessageByStomp(chatMessageDTO);
    }

    // 2. kafka + stomp
    @MessageMapping("kafka/{chatRoomId}")
    public void sendMessageByKafka(
            @DestinationVariable String chatRoomId,
            ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor // ws 연결된 사용자 세션정보(사용자 정보)
    ) {
        commandChatMessageService.sendMessageByKafka(chatMessageDTO);
    }

    // 3. kafka + stomp + idempotencyKey
    @MessageMapping("kafka/idempotencyKey/{chatRoomId}")
    public void sendMessageByKafkaIdempotencyKey(
            @DestinationVariable String chatRoomId,
            ChatMessageIdempotencyKeyDTO chatMessageIdempotencyKeyDTO,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        log.info("Sending message by kafka/idempotencyKey: {}", chatMessageIdempotencyKeyDTO.getMessage());
        commandChatMessageService.sendMessageByKafkaIdempotencyKey(chatMessageIdempotencyKeyDTO);
    }

    // 4. kfaka + stomp + idempotencyKey + outbox
    @MessageMapping("kafka/idempotencyKey/outbox/{chatRoomId}")
    public void sendMessageByKafkaIdempotencyKeyOutbox(
            @DestinationVariable String chatRoomId,
            ChatMessageIdempotencyKeyDTO chatMessageIdempotencyKeyDTO,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        log.info("Sending message by kafka/idempotencyKey/outbox: {}", chatMessageIdempotencyKeyDTO.getMessage());
        commandChatMessageService.sendMessageByKafkaIdempotencyKeyOutbox(chatMessageIdempotencyKeyDTO);
    }
}
