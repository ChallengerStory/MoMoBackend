package challengers.momochat.chat.command.domain.service;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.service.CommandChatMessageService;
import challengers.momochat.chat.command.domain.aggregate.entity.ChatMessage;
import challengers.momochat.chat.command.domain.repository.CommandChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandChatMessageServiceImpl implements CommandChatMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CommandChatMessageRepository commandChatMessageRepository;

    @Transactional
    @Override
    public void sendMessageByStomp(ChatMessageDTO chatMessageDTO) {

        // 엔티티로 변환
        ChatMessage chatMessage = ChatMessage.builder()
                .chatMessageId(null)  // 생략 가능
                .chatRoomId(chatMessageDTO.getChatRoomId())
                .memberId(chatMessageDTO.getMemberId())
                .memberNickname(chatMessageDTO.getMemberNickname())
                .memberPhoto(chatMessageDTO.getMemberPhoto())
                .message(chatMessageDTO.getMessage())
                .messageType(chatMessageDTO.getMessageType())
                .unreadCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commandChatMessageRepository.save(chatMessage);

        simpMessagingTemplate.convertAndSend("/topic/message/"
                + chatMessageDTO.getChatRoomId(), chatMessageDTO);
    }

    @Transactional
    @Override
    public void sendMessageByKafka(ChatMessageDTO chatMessageDTO){



    }

    @Transactional
    @Override
    public void sendMessage(ChatMessageDTO chatMessageDTO) {

        // 1. 멱등성 체크

        // 2. 전송 메시지 db 저장

        // 3. 도메인 이벤트(채팅 전달됨)

        // 4. 이벤트 직렬화

        // 5. outbox 엔티티 생성 + 멱등키를 통한 중복 방지

        // 6. outbox 등록

        // 7. 실패시 레디스 키 삭제 + 트랜잭션 롤백

    }

}
