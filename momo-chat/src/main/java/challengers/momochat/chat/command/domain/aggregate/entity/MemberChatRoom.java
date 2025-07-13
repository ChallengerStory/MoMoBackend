package challengers.momochat.chat.command.domain.aggregate.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity(name = "memberChatRoom")
//@Table(name = "member_chat_room")
//public class MemberChatRoom { // RDB에 저장
//
//    @Id
//    private String memberChatRoomId;
//
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name="member_id")
////    private Member member;
//    private String memberId; // 회원의 경우에는 아직 연결을 안함.
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "chat_room_id")
//    private ChatRoom chatRoom;
////    private String chatRoomId;
//}
