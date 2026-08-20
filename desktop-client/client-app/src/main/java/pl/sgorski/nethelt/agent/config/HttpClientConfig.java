package pl.sgorski.nethelt.agent.config;

import java.time.Duration;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

  private static final Duration CALL_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

  @Bean
  public OkHttpClient webServerClient() {
    return new OkHttpClient()
        .newBuilder()
        .callTimeout(CALL_TIMEOUT)
        .connectTimeout(CONNECT_TIMEOUT)
        .readTimeout(READ_TIMEOUT)
        .build();
  }
}
