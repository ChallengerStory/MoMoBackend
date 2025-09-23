package challengers.momochat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MomoChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomoChatApplication.class, args);
    }

}
