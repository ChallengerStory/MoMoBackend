package challengers.momochat.chat.command.application.dto;

import challengers.momochat.chat.command.domain.aggregate.enums.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageIdempotencyKeyDTO {

    private String chatRoomId;
    private String memberId;
    private String memberNickname;
    private String memberPhoto;
    private String message;
    private ChatMessageType messageType;
    private String idempotencyKey;
}
