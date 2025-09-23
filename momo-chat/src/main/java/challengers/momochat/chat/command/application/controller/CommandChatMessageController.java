package challengers.momochat.chat.command.application.controller;

import challengers.momochat.chat.command.application.service.CommandChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-message")
@RequiredArgsConstructor
public class CommandChatMessageController {
    // command는 cud에 대한 것들의 엔드포인트를 제공해줌

    private final CommandChatMessageService commandChatMessageService;

    // 어떤게 있어야할까? 채팅 메시지를 보내는것?? -> 이건 ws controller에서 처리한다.
    // 어떤게 있어야할지 생각해보기


}
