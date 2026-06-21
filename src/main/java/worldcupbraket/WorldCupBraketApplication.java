package worldcupbraket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorldCupBraketApplication {

    static void main(String[] args) {
        SpringApplication.run(WorldCupBraketApplication.class, args);
    }
}
