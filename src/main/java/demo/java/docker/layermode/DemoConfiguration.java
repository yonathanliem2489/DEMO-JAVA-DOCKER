package demo.java.docker.layermode;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class DemoConfiguration {

  @Bean
  DemoScheduler demoScheduler() {
    return new DemoScheduler();
  }

  private static class DemoScheduler {
    Logger logger = LoggerFactory.getLogger(DemoScheduler.class);

    @Scheduled(cron = "* * * * * *")
    public void scheduler() {
      logger.info("demo scheduler run on {}", LocalDateTime.now());
    }

  }

}
