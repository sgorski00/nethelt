package pl.sgorski.nethelt.webapi.security.oauth2;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Service
@RequiredArgsConstructor
public final class OAuth2ContextCookieService {

  public static final String COOKIE_NAME = "oauth2_ctx";
  public static final Duration COOKIE_EXPIRATION = Duration.ofMinutes(5);

  private final CookieService cookieService;

  public void clearContext() {
    cookieService.delete(COOKIE_NAME);
  }

  public void saveContext(String token) {
    cookieService.save(COOKIE_NAME, token, COOKIE_EXPIRATION);
  }

  public Optional<String> readContext() {
    return cookieService.find(COOKIE_NAME);
  }
}
