package pl.sgorski.nethelt.webapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Service
@RequiredArgsConstructor
// TODO: refactor. change it to AccessTokenService and move jwt logic to the JwtService.
// OAuth2ContextSerivce should user JwtService too.
public final class JwtService {

  private final AuthProperties authProperties;
  private final SecretKey secretKey;

  public String generateAccessToken(User user) {
    var now = Instant.now();
    var expirationTime = now.plus(authProperties.jwtTokenExpiration());
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
