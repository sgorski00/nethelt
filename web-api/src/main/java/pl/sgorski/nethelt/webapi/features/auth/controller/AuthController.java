package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@RestController
@RequestMapping(value = "/auth", version = "1")
@RequiredArgsConstructor
public final class AuthController {
  private final LocalAuthService localAuthService;
  private final AuthMapper authMapper;
  private final UserMapper userMapper;
  private final RefreshTokenService refreshTokenService;
  private final CookieService cookieService;
  private final AuthProperties authProperties;
  private final AccessTokenService accessTokenService;

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
    var user = localAuthService.login(authMapper.toCommand(request));
    var body = new JwtResponse(accessTokenService.generateAccessToken(user));
    return buildResponseWithRefreshToken(user, HttpStatus.OK, body);
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
    var command = authMapper.toCommand(request);
    var user = localAuthService.registerUser(command);
    return buildResponseWithRefreshToken(user, HttpStatus.CREATED, userMapper.toResponse(user));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refreshToken(
      @CookieValue(CookieNames.REFRESH_TOKEN) String refreshTokenCookie) {
    var user = refreshTokenService.validateAndGetUser(refreshTokenCookie);
    refreshTokenService.revokeToken(refreshTokenCookie);
    var body = new JwtResponse(accessTokenService.generateAccessToken(user));
    return buildResponseWithRefreshToken(user, HttpStatus.OK, body);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(value = CookieNames.REFRESH_TOKEN, required = false)
          @Nullable String refreshTokenCookie) {
    if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
      refreshTokenService.revokeToken(refreshTokenCookie);
    }
    cookieService.delete(CookieNames.REFRESH_TOKEN);
    return ResponseEntity.noContent().build();
  }

  private <T> ResponseEntity<T> buildResponseWithRefreshToken(
      User user, HttpStatus status, T body) {
    var value = refreshTokenService.generateRefreshToken(user).getToken();
    cookieService.save(CookieNames.REFRESH_TOKEN, value, authProperties.refreshTokenExpiration());
    return ResponseEntity.status(status).body(body);
  }
}
