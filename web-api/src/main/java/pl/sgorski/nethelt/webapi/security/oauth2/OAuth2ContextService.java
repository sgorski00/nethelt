package pl.sgorski.nethelt.webapi.security.oauth2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class OAuth2ContextService {

  private static final Duration OAUTH2_CONTEXT_EXPIRATION = Duration.ofMinutes(5);
  private static final String MODE_CLAIM_KEY = "mode";

  private final SecretKey secretKey;

  public String generateAccessToken(Long userId, OAuth2Mode mode) {
    var now = Instant.now();
    var expirationTime = now.plus(OAUTH2_CONTEXT_EXPIRATION);
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim(MODE_CLAIM_KEY, mode.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expirationTime))
        .signWith(this.secretKey)
        .compact();
  }

  public OAuth2ContextPayload parse(String token) {
    var claims = getClaimsFromToken(token);
    var userId = Long.valueOf(claims.getSubject());
    var mode = OAuth2Mode.fromString(claims.get(MODE_CLAIM_KEY, String.class));
    return new OAuth2ContextPayload(userId, mode);
  }

  private Claims getClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
  }
}
