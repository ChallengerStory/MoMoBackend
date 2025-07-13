package challengers.momochat.chat;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import challengers.momochat.chat.command.application.dto.ChatRoomDTO;
import challengers.momochat.chat.command.domain.aggregate.entity.ChatMessage;
import challengers.momochat.chat.command.domain.repository.CommandChatMessageRepository;
import challengers.momochat.chat.command.domain.repository.CommandChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final CommandChatMessageRepository commandChatMessageRepository;

    public TestController( CommandChatMessageRepository commandChatMessageRepository) {
        this.commandChatMessageRepository = commandChatMessageRepository;
    }

    @PostMapping("/insert/message")
    public HttpEntity<ChatMessageDTO> insert(
            @RequestBody ChatMessageDTO chatMessageDTO
    ) {
            /*
            *
            * 1. chatMessageDTO
            private String chatRoomId;
            private String memberId;
            private String memberNickname;
            private String memberPhoto; // 이건 뭐지?
            private String message;
            private ChatMessageType messageType;
            *
            *
            * */
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoomId(chatMessageDTO.getChatRoomId());
        chatMessage.setMemberId(chatMessageDTO.getMemberId());
        chatMessage.setMemberNickname(chatMessageDTO.getMemberNickname());
        chatMessage.setMessage(chatMessageDTO.getMessage());
        chatMessage.setMessageType(chatMessageDTO.getMessageType());
        commandChatMessageRepository.save(chatMessage);

        return new HttpEntity<>(chatMessageDTO);
    }
//
//    @PostMapping("/insert/room")
//    public ResponseEntity<String> insertRoom(
//            @RequestBody ChatRoomDTO chatRoomDTO
//    ){
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.createId();
//        chatRoom.setChatRoomName(chatRoomDTO.getChatRoomName());
////        commandChatRoomRepository.save(chatRoom);
//
////            * 2. chatRoomDTO
////        private String chatRoomName;
////        //    private Boolean isActive; // 방이 활성화 상태인지
////
////        //    private LocalDateTime createdAt;
////        //    private LocalDateTime updatedAt;
////        private LocalDateTime lastMessageTime;
////        private String chatRoomPhoto;
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(chatRoom.getChatRoomId());
//    }
}
