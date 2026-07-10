package pl.sgorski.nethelt.webapi.features.auth.helper;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.config.RefreshTokenProperties;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.security.jwt.JwtService;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Component
@RequiredArgsConstructor
public class TokenResponseEntityCreator {

  private final JwtService jwtService;
  private final RefreshTokenProperties refreshTokenProperties;
  private final RefreshTokenService refreshTokenService;
  private final CookieService cookieService;
  private final UserMapper userMapper;

  public static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";
  private static final HttpStatus AUTH_SUCCESS_STATUS = HttpStatus.OK;
  private static final HttpStatus REGISTRATION_STATUS = HttpStatus.CREATED;

  public ResponseEntity<JwtResponse> createTokenResponse(User user) {
    return buildResponse(
        user, AUTH_SUCCESS_STATUS, new JwtResponse(jwtService.generateAccessToken(user)));
  }

  public ResponseEntity<UserResponse> createRegistrationResponse(User user) {
    return buildResponse(user, REGISTRATION_STATUS, userMapper.toResponse(user));
  }

  public ResponseEntity<JwtResponse> createOAuth2Response(User user) {
    return createTokenResponse(user);
  }

  public ResponseEntity<Void> createClearResponse() {
    cookieService.delete(REFRESH_TOKEN_COOKIE_KEY);
    return ResponseEntity.noContent().build();
  }

  private <T> ResponseEntity<T> buildResponse(User user, HttpStatus status, T body) {
    var value = refreshTokenService.generateRefreshToken(user).getToken();
    var expiration = Duration.ofMillis(refreshTokenProperties.expirationTimeInMs());
    cookieService.save(REFRESH_TOKEN_COOKIE_KEY, value, expiration);
    return ResponseEntity.status(status).body(body);
  }
}
