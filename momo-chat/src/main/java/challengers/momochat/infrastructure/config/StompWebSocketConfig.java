package challengers.momochat.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    // 웹소켓 연결을 위한 엔드포인트 제공
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins("*") // 임시
                .withSockJS();
    }

    @Override
    // 채팅 메시지 브로커 설정 (pub/sub simple broker 사용)
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // pub (클라이언트 -> 서버) 메시지 전송. broadcast
        registry.setApplicationDestinationPrefixes("/app");

        // sub (서버 -> 클라이언트)
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                .setMessageSizeLimit(8192)
                .setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(3 * 512 * 1024);
    }

}
