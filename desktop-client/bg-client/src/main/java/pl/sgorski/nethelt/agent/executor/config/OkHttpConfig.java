package pl.sgorski.nethelt.agent.executor.config;

import java.time.Duration;
import java.util.Objects;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {

  static final String USER_AGENT =
      "NetHelt-Agent/"
          + Objects.requireNonNullElse(
              OkHttpConfig.class.getPackage().getImplementationVersion(), "dev");

  @Bean
  OkHttpClient healthcheckHttpClient() {
    return new OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(false)
        .retryOnConnectionFailure(false)
        .addInterceptor(
            chain ->
                chain.proceed(
                    chain.request().newBuilder().header("User-Agent", USER_AGENT).build()))
        .build();
  }

  @Bean
  DisposableBean okHttpClientShutdown(OkHttpClient client) {
    return () -> {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    };
  }
}
