package challengers.momochat.chat.command.domain.aggregate.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ChatMessageType {
    ENTER,
    LEAVE,
    CHAT,
}
