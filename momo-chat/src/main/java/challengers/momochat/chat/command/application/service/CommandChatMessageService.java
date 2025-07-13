package challengers.momochat.chat.command.application.service;

import challengers.momochat.chat.command.application.dto.ChatMessageDTO;
import org.springframework.transaction.annotation.Transactional;

public interface CommandChatMessageService {

    @Transactional
    void sendMessageByStomp(ChatMessageDTO chatMessageDTO);

    @Transactional
    void sendMessageByKafka(ChatMessageDTO chatMessageDTO);

    public void sendMessage(ChatMessageDTO chatMessageDTO);
}
