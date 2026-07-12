package pl.sgorski.nethelt.webapi.security.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final SecretKey secretKey;

  public String generate(JwtPayload payload) {
    var now = Instant.now();
    return Jwts.builder()
        .subject(payload.subject())
        .claims(payload.claims())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(payload.expiration())))
        .signWith(this.secretKey)
        .compact();
  }

  public boolean isValid(String token) {
    try {
      var claims = getClaimsFromToken(token);
      return claims.getExpiration().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public String getSubject(String token) {
    var claims = getClaimsFromToken(token);
    return claims.getSubject();
  }

  public <T> T getClaim(String token, String claim, Class<T> type) {
    var claims = getClaimsFromToken(token);
    return claims.get(claim, type);
  }

  private Claims getClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
  }
}
