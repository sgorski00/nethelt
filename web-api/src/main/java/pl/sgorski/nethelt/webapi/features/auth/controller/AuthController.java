package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.features.auth.service.CookieResponseHelper;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;
import pl.sgorski.nethelt.webapi.security.jwt.JwtService;

@RestController
@RequestMapping(value = "/auth", version = "1")
@RequiredArgsConstructor
public final class AuthController {
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final LocalAuthService localAuthService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieResponseHelper cookieResponseHelper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        var user = localAuthService.login(authMapper.toCommand(request));
        var jwtToken = new JwtResponse(jwtService.generateAccessToken(user));
        var refreshToken = refreshTokenService.generateRefreshToken(user);
        var cookie = cookieResponseHelper.createRefreshTokenCookie(
                refreshToken.getToken(),
                refreshTokenService.getExpirationSecond());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(jwtToken);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegisterUserRequest request
    ) {
        var command = authMapper.toCommand(request);
        var user = localAuthService.registerUser(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @CookieValue(CookieResponseHelper.REFRESH_TOKEN_COOKIE_KEY) String refreshTokenCookie,
            Authentication authentication
    ) {
        var userId = authenticatedUserResolver.requireUserId(authentication);
        var user = userService.getUser(userId);
        refreshTokenService.validateToken(refreshTokenCookie, user);
        refreshTokenService.revokeToken(refreshTokenCookie);
        var refreshToken = refreshTokenService.generateRefreshToken(user);
        var cookie = cookieResponseHelper.createRefreshTokenCookie(
                refreshToken.getToken(),
                refreshTokenService.getExpirationSecond());
        var jwtTokenStr = jwtService.generateAccessToken(user);
        var jwtToken = new JwtResponse(jwtTokenStr);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,  cookie.toString())
                .body(jwtToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = CookieResponseHelper.REFRESH_TOKEN_COOKIE_KEY, required = false) String refreshTokenCookie
    ) {
        var cookie = cookieResponseHelper.createClearRefreshTokenCookie();
        refreshTokenService.revokeToken(refreshTokenCookie);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
