package challengers.momochat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // key, value 역직렬화 어캐할지 정한거
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 이거는 컨슈머마다 붙는 group id인데 스프링 서버 새로 띄울때마다 달라져야하는거 아닌가?
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // 오프셋 기준 설정
        // - earliest: 이전에 이 컨슈머가 기록한 offset이 없다면 가장 처음부터 읽음
        // - latest: 이전 기록 없으면 최신 오프셋부터 읽음
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 자동 커밋 비활성화
        // - false일 경우 Listener 내부에서 ack.acknowledge()로 직접 커밋해야 함
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 한번 poll() 호출시에 가져올 최대 메시지 개수
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

        // 컨슈머가 응답 없을 시에 세션 종료까지 기다리는 시간 ms
        // kafka는 이 시간 내로 heartbeat가 오지 않으면 해당 컨슈머를 죽었다고 판단
        // 기본값은 10000
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);

        ObjectMapper objectMapper = new ObjectMapper();

        // 역직렬화를 위한 deserializer
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class, objectMapper);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    // dlt topic으로 보내는 카프카 프로듀서 구성
    @Bean
    public ProducerFactory<String, Object> dltProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(props);
    }

    // 카프카 dlt topic으로 보내기 위한 템플릿
    @Bean
    public KafkaTemplate<String, Object> kafkaDltTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    // 에러 헨들러: 재시도 + recoverer 사용
    @Bean
    public DefaultErrorHandler errorHandler(
            @Qualifier("kafkaDltTemplate") KafkaTemplate<String, Object> kafkaConsumerTemplate
            // kafkaTemplate<String, Object>가 2개라 빈 추론 에러 -> Qualifier 사용
    ) {

        // 백오프 설정: 최대 3번, 200ms → 400ms → 최대 1000ms
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(200L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(1000L);

        // Recoverer 설정 (DLT 전송용)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaConsumerTemplate,
                (record, exception) -> {
                    log.error("DLT로 메시지 전송: 토픽 = {}, 파티션 = {}, 오프셋 = {}, 예외 = {}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage());
                    return new TopicPartition("chat.outbox.consumer.dlq", record.partition());
                }
        );

        return new DefaultErrorHandler(recoverer, backOff);

//        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2)); // 2초 간격 2회 재시도
    }

    // 리스너(컨슈머)가 실제로 만들어지고 운영되는 공장
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.setConcurrency(1); // consumer thread 수, default는 1 -> 파티션 개수만큼 병렬처리 해줘야함.
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}

