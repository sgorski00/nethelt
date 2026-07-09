package pl.sgorski.nethelt.webapi.features.auth.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

@Component
@RequiredArgsConstructor
public class TokenResponseEntityCreator {

  private final JwtService jwtService;
  private final RefreshTokenProperties refreshTokenProperties;
  private final RefreshTokenService refreshTokenService;
  private final CookieResponseHelper cookieResponseHelper;
  private final UserMapper userMapper;

  private static final HttpStatus AUTH_SUCCESS_STATUS = HttpStatus.OK;
  private static final HttpStatus REGISTRATION_STATUS = HttpStatus.CREATED;

  /**
   * Creates successful login/refresh response with JWT in body and refresh cookie.
   *
   * @param user the authenticated user
   * @return ResponseEntity with JWT in body and refresh token cookie
   */
  public ResponseEntity<JwtResponse> createTokenResponse(User user) {
    return buildResponse(
        user, AUTH_SUCCESS_STATUS, new JwtResponse(jwtService.generateAccessToken(user)));
  }

  /**
   * Creates registration response with user data in body and refresh token cookie.
   *
   * @param user the newly created user
   * @return ResponseEntity with user details and refresh token cookie
   */
  public ResponseEntity<UserResponse> createRegistrationResponse(User user) {
    return buildResponse(user, REGISTRATION_STATUS, userMapper.toResponse(user));
  }

  /**
   * Creates OAuth2 login response with JWT and refresh token. Used for OAuth2 success handler
   * callback.
   *
   * @param user the user from OAuth2 provider
   * @return ResponseEntity with JWT and refresh token cookie
   */
  public ResponseEntity<JwtResponse> createOAuth2Response(User user) {
    return createTokenResponse(user);
  }

  /**
   * Creates response that clears the refresh token cookie.
   *
   * @return ResponseEntity with no content and cleared refresh token cookie
   */
  public ResponseEntity<Void> createClearResponse() {
    var cookie = cookieResponseHelper.createClearRefreshTokenCookie();
    return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
  }

  private <T> ResponseEntity<T> buildResponse(User user, HttpStatus status, T body) {
    var cookie = createRefreshTokenCookie(user);
    return ResponseEntity.status(status)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(body);
  }

  private org.springframework.http.ResponseCookie createRefreshTokenCookie(User user) {
    var refreshToken = refreshTokenService.generateRefreshToken(user);
    return cookieResponseHelper.createRefreshTokenCookie(
        refreshToken.getToken(), refreshTokenProperties.expirationTimeInMs() / 1000);
  }
}
