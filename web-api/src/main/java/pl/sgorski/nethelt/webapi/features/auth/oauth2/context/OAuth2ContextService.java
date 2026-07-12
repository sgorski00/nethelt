package pl.sgorski.nethelt.webapi.features.auth.oauth2.context;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.security.token.JwtPayload;
import pl.sgorski.nethelt.webapi.security.token.JwtService;

@Service
@RequiredArgsConstructor
public final class OAuth2ContextService {

  private static final String MODE_CLAIM_KEY = "mode";

  private final JwtService jwtService;

  public String generateContextToken(Long userId, OAuth2Mode mode, Duration expiration) {
    var claims = Map.of(MODE_CLAIM_KEY, mode.name());
    var payload = new JwtPayload(userId.toString(), claims, expiration);
    return jwtService.generate(payload);
  }

  public OAuth2ContextPayload parse(String token) {
    var userId = Long.valueOf(jwtService.getSubject(token));
    var mode = OAuth2Mode.fromString(jwtService.getClaim(token, MODE_CLAIM_KEY, String.class));
    return new OAuth2ContextPayload(userId, mode);
  }
}
