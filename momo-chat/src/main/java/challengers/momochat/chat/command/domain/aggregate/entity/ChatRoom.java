package challengers.momochat.chat.command.domain.aggregate.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity(name = "chatRoom")
//@Table(name = "chat_room")
//public class ChatRoom { // RDB에 저장
//    @Id
//    private String chatRoomId;
//
//    private String chatRoomName;
//    private Boolean isActive; // 방이 활성화 상태인지
//
//    @CreatedDate
//    private LocalDateTime createdAt;
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//    private LocalDateTime lastMessageTime;
//    private String chatRoomPhoto;
//
//    public void createId() {
//        String prefix = "ROOM-";
//        UUID uuid = UUID.randomUUID();
//        this.chatRoomId = prefix + uuid.toString();
//    }
//}
