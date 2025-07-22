package challengers.momochat.infrastructure.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration
public class EarlyEnvCheck {

    private static final Logger log = LoggerFactory.getLogger(EarlyEnvCheck.class);
    private final Environment env;

    public EarlyEnvCheck(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void checkMongoUri() {
        String uri = env.getProperty("spring.data.mongodb.uri");
        System.out.println("📌 Mongo URI = " + uri); // 강제 출력
    }
}

