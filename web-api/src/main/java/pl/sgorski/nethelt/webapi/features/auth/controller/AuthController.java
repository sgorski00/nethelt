package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.service.CookieResponseHelper;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.auth.service.TokenResponseEntityCreator;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;

@RestController
@RequestMapping(value = "/auth", version = "1")
@RequiredArgsConstructor
public final class AuthController {
  private final LocalAuthService localAuthService;
  private final AuthMapper authMapper;
  private final RefreshTokenService refreshTokenService;
  private final TokenResponseEntityCreator tokenResponseCreator;

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
    var user = localAuthService.login(authMapper.toCommand(request));
    return tokenResponseCreator.createTokenResponse(user);
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
    var command = authMapper.toCommand(request);
    var user = localAuthService.registerUser(command);
    return tokenResponseCreator.createRegistrationResponse(user);
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refreshToken(
      @CookieValue(CookieResponseHelper.REFRESH_TOKEN_COOKIE_KEY) String refreshTokenCookie) {
    var user = refreshTokenService.validateAndGetUser(refreshTokenCookie);
    refreshTokenService.revokeToken(refreshTokenCookie);
    return tokenResponseCreator.createTokenResponse(user);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(value = CookieResponseHelper.REFRESH_TOKEN_COOKIE_KEY, required = false)
          @Nullable String refreshTokenCookie) {
    if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
      refreshTokenService.revokeToken(refreshTokenCookie);
    }
    return tokenResponseCreator.createLogoutResponse();
  }
}
