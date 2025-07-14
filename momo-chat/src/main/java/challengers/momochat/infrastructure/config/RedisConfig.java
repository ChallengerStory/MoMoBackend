package challengers.momochat.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 직렬화 역직렬화만 있는 redis config
    // 추후 HA를 위한 설정 추가
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key serializer
        template.setKeySerializer(new StringRedisSerializer());

        // value serializer (예: JSON 직렬화)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}

