package challengers.momochat.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${REDIS_MASTER}")
    private String redisMaster;

    @Value("${REDIS_PORT1}")
    private String redisPort1;

    @Value("${REDIS_PORT2}")
    private String redisPort2;

    @Value("${REDIS_PORT3}")
    private String redisPort3;

    @Value("${DB_PASSWORD}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.master(redisMaster);

        log.info("redisMaster: {}", redisMaster);

        sentinelConfig.sentinel(parseHost(redisPort1), parsePort(redisPort1));
        sentinelConfig.sentinel(parseHost(redisPort2), parsePort(redisPort2));
        sentinelConfig.sentinel(parseHost(redisPort3), parsePort(redisPort3));

        sentinelConfig.setPassword(RedisPassword.of(redisPassword));
        return new LettuceConnectionFactory(sentinelConfig);
    }

    private String parseHost(String hostPort) {
        String[] parts = hostPort.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("sentinel 주소가 잘못되었습니다: " + hostPort);
        return parts[0];
    }

    private int parsePort(String hostPort) {
        String[] parts = hostPort.split(":");
        return Integer.parseInt(parts[1]);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // ObjectMapper 설정
        ObjectMapper mapper = new ObjectMapper();

        // Java 8의 날짜/시간 API (LocalDateTime 등) 직렬화/역직렬화 지원
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 timestamp가 아니라 문자열(ISO 8601)로 저장하게 설정
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 필드 제외
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 알 수 없는 필드가 있어도 무시하고 역직렬화
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 타입 정보 포함 (역직렬화 시 타입 보존용)
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );

        // Redis value serializer로 설정 (Jackson 기반 JSON 직렬화)
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        // key/value/hash 관련 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        // 모든 속성 설정 완료 후 초기화
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5));
        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }
}

