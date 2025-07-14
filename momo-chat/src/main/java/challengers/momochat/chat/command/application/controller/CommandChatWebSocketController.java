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

    @MessageMapping("stomp/{chatRoomId}") // stomp로 메시지 전송
    public void sendMessageByStomp(
            @DestinationVariable String chatRoomId,
            ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor // ws 연결된 사용자 세션정보(사용자 정보)
    ) {
        log.info("Sending message: {}", chatMessageDTO);

        commandChatMessageService.sendMessageByStomp(chatMessageDTO);
    }

    @MessageMapping("kafka/{chatRoomId}")
    public void sendMessageByKafka(
            @DestinationVariable String chatRoomId,
            ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor // ws 연결된 사용자 세션정보(사용자 정보)
    ){

        commandChatMessageService.sendMessageByKafka(chatMessageDTO);
    }

    @MessageMapping("kafka/idempotencyKey/{chatRoomId}")
    public void sendMessageByKafkaIdempotencyKey(
            @DestinationVariable String chatRoomId,
            ChatMessageIdempotencyKeyDTO chatMessageIdempotencyKeyDTO,
            SimpMessageHeaderAccessor headerAccessor // ws 연결된 사용자 세션정보(사용자 정보)
    ){
        // 헤더에 멱등키 추가

        commandChatMessageService.sendMessageByKafkaIdempotencyKey(chatMessageIdempotencyKeyDTO);

    }

//    @MessageMapping("{chatRoomId}")
//    fun sendMessageByKafkaOutbox(
//            @DestinationVariable chatRoomId: String,
//            @Header("idempotencyKey") idempotencyKey: String,
//            @Payload sendMessageRequestVo: SendMessageRequestVo,
//            simpleMessageHeaderAccessor: SimpMessageHeaderAccessor,
//            ) {
//        println("idempotencyKey:$idempotencyKey")
//        // Vo -> Dto + 토큰에서 memberId/Nickname/Photo를 받아올 수 있도록 수정해야 함
//        val commandChatMessageKeyRequestDTO =
//                voToDtoConverter.sendMessageRequestVoAndKeyToDTO(
//                        sendMessageRequestVo,
//                        chatRoomId,
//                        idempotencyKey,
//                        )
//
//        // messageType이 Enter일 경우에는 처음 등장이므로, Websocket의 세션에 정보 저장 (simpleMessageHeaderAccessor)
//        val updateMessageDTO =
//                when (commandChatMessageKeyRequestDTO.messageType) {
//            ChatMessageType.ENTER -> {
//                simpleMessageHeaderAccessor.sessionAttributes?.apply {
//                    // 향후 Token 완성 후 수정필요
//                    put("memberId", commandChatMessageKeyRequestDTO.memberId)
//                    put("memberNickname", commandChatMessageKeyRequestDTO.memberNickname)
//                    put("memberPhoto", commandChatMessageKeyRequestDTO.memberPhoto)
//                    put("chatRoomId", commandChatMessageKeyRequestDTO.chatRoomId)
//                }
//
//                commandChatMessageKeyRequestDTO.copy(message = "${commandChatMessageKeyRequestDTO.memberNickname}님이 입장하셨습니다.")
//            }
//                else -> commandChatMessageKeyRequestDTO
//        }
//
//        // 메시지 전송
//        commandChatMessageService.sendMessageByKafkaOutbox(updateMessageDTO)
//    }
}
