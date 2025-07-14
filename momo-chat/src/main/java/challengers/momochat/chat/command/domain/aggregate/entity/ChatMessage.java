package challengers.momochat.chat.command.domain.aggregate.entity;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.domain.aggregate.enums.ChatMessageType;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    /**
     * chatMessageId
     * chatRoomId
     * String memberId
     * memberNickname
     * memberPhoto
     * message
     * messageType
     * unreadCount
     * createdAt
     * updatedAt
     */
    @Id
    private ObjectId chatMessageId;
    private String chatRoomId;
    private String memberId;
    private String memberNickname;
    private String memberPhoto;
    private String message;
//    @Enumerated(EnumType.STRING)
    private ChatMessageType messageType;
    private Integer unreadCount;

    @CreatedDate
    private LocalDateTime createdAt; // Instant 고려 -> 몽고디비 시간과 호환성?

    @LastModifiedDate
    private LocalDateTime updatedAt;


    // ChatMessageDTO -> ENTITY 정적 팩토리 메서드
    public static ChatMessage fromDTO(ChatMessageDTO dto){
        return ChatMessage.builder()
                .chatRoomId(dto.getChatRoomId())
                .memberId(dto.getMemberId())
                .memberNickname(dto.getMemberNickname())
                .memberPhoto(dto.getMemberPhoto())
                .message(dto.getMessage())
                .messageType(dto.getMessageType())
                .unreadCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
