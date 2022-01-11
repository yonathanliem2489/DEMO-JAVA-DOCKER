package demo.java.docker.layermode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class LayerModeApplication {


	public static void main(String[] args) {
		SpringApplication.run(LayerModeApplication.class, args);
	}




}
