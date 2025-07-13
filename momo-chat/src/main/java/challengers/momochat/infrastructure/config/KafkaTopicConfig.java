package challengers.momochat.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
//@Profile("dev") // 개발 단계에서 테스트를 위한 활성화
@RequiredArgsConstructor
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.chat}")
    private String topicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic chatTopic() {
        // 운영 환경에서는 각 토픽 데이터를 3개의 Broker에 복제하여 장애 발생 시 데이터 내구성을 확보합니다.
        return TopicBuilder.name(topicName)
                .partitions(1)
//                .replicas(3)
                .replicas(1)
                .build();
    }
}
