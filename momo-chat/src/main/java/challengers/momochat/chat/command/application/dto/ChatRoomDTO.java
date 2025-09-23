package challengers.momochat.chat.command.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private String chatRoomName;
//    private Boolean isActive; // 방이 활성화 상태인지

//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageTime;
    private String chatRoomPhoto;
}
