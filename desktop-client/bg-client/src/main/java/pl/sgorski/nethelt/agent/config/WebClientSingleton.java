package pl.sgorski.nethelt.agent.config;

import java.time.Duration;

import lombok.Getter;
import okhttp3.OkHttpClient;

/**
 * Singleton OkHttpClient instance with configured timeouts.
 */
@Getter
public enum WebClientSingleton {
  INSTANCE;

  private final OkHttpClient httpClient = createHttpClient();
  private static final Duration CALL_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

  private static OkHttpClient createHttpClient() {
    return new OkHttpClient().newBuilder()
      .callTimeout(CALL_TIMEOUT)
      .connectTimeout(CONNECT_TIMEOUT)
      .readTimeout(READ_TIMEOUT)
      .build();
  }
}
