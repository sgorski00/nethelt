package pl.sgorski.nethelt.webapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Service
public final class JwtService {

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey =
        Keys.hmacShaKeyFor(this.jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user) {
    var now = Instant.now();
    var expirationTime = now.plusMillis(jwtProperties.expirationTimeInMs());
    var authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .claim("email", user.getEmail())
        .claim("roles", authorities)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expirationTime))
        .signWith(this.secretKey)
        .compact();
  }

  public boolean isTokenValid(String token) {
    try {
      var claims = getClaimsFromToken(token);
      return claims.getExpiration().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public String getEmailFromToken(String token) {
    return getClaimsFromToken(token).get("email", String.class);
  }

  private Claims getClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
  }
}
