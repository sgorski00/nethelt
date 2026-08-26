package pl.sgorski.nethelt.agent.executor.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringExecutorConfig {

  @Bean
  public ExecutorService monitoringExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
