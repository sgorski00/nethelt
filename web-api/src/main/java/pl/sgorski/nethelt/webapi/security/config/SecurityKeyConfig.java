package pl.sgorski.nethelt.webapi.security.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;

@Configuration
@RequiredArgsConstructor
public class SecurityKeyConfig {

  private final AuthProperties authProperties;

  @Bean
  public SecretKey secretKey() {
    return Keys.hmacShaKeyFor(authProperties.jwtSecretKey().getBytes(StandardCharsets.UTF_8));
  }
}
