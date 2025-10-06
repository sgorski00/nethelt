package pl.gorski.nethelt.agent.config;

import java.time.Duration;
import okhttp3.OkHttpClient;

/**
 * Singleton OkHttpClient instance with configured timeouts.
 */
public class WebClientSingleton {

  private static final OkHttpClient INSTANCE;

  private static final Duration CALL_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

  static {
    INSTANCE = new OkHttpClient().newBuilder()
      .callTimeout(CALL_TIMEOUT)
      .connectTimeout(CONNECT_TIMEOUT)
      .readTimeout(READ_TIMEOUT)
      .build();
  }

  public static OkHttpClient getInstance() {
    return INSTANCE;
  }
}
