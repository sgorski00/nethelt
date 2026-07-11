package pl.sgorski.nethelt.webapi.features.auth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.security.jwt.JwtPayload;
import pl.sgorski.nethelt.webapi.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

  private final AuthProperties authProperties;
  private final JwtService jwtService;

  public String generateAccessToken(User user) {
    var subject = user.getId().toString();
    var authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    var claims = Map.of("email", user.getEmail(), "roles", authorities);
    var payload = new JwtPayload(subject, claims, authProperties.jwtTokenExpiration());
    return jwtService.generate(payload);
  }

  public String getEmailFromToken(String token) {
    return jwtService.getClaim(token, "email", String.class);
  }

  public boolean isValid(String token) {
    return jwtService.isValid(token);
  }
}
