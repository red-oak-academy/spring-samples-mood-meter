package academy.redoak.samples.moodmeter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoodmeterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoodmeterApplication.class, args);
	}
}
