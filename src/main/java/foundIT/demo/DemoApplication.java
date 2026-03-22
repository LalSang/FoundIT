package foundIT.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

	/** Boots the Spring application and starts the web server. */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
