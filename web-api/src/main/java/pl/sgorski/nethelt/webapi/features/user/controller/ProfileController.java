package pl.sgorski.nethelt.webapi.features.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordChangeRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordSetRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileCreateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileUpdateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.DetailedUserResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.ProfileResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.ProfileMapper;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.service.ProfileService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2ContextCookieService;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2ContextService;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2Mode;

@Log4j2
@RestController
@RequestMapping(path = "/profile", version = "1")
@RequiredArgsConstructor
public final class ProfileController {

  private final LocalAuthService localAuthService;
  private final UserService userService;
  private final UserMapper userMapper;
  private final ProfileMapper profileMapper;
  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final OAuth2ContextService oAuth2ContextService;
  private final OAuth2ContextCookieService oAuth2ContextCookieService;
  private final ProfileService profileService;

  @GetMapping
  public ResponseEntity<DetailedUserResponse> showProfile(Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUserWithProfileAndIdentities(userId);
    return ResponseEntity.ok(userMapper.toDetailedResponse(user));
  }

  @PostMapping
  public ResponseEntity<ProfileResponse> createProfile(
      @RequestBody ProfileCreateRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var command = profileMapper.toCreateCommand(userId, request);
    var profile = profileService.createProfile(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(profileMapper.toProfileResponse(profile));
  }

  @PutMapping
  public ResponseEntity<ProfileResponse> updateProfile(
      @RequestBody ProfileUpdateRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var command = profileMapper.toUpdateCommand(userId, request);
    var profile = profileService.updateProfile(command);
    return ResponseEntity.ok(profileMapper.toProfileResponse(profile));
  }

  @PutMapping("/password")
  public ResponseEntity<Void> setLocalPassword(
      @RequestBody @Valid PasswordSetRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUser(userId);
    localAuthService.setLocalPassword(user, request.newPassword());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/password")
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid PasswordChangeRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUser(userId);
    localAuthService.changePassword(user, request.oldPassword(), request.newPassword());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/link/{provider}")
  public ResponseEntity<Void> prepareCookiesForOauthLinking(
      @SuppressWarnings("SpringMvcPathVariableDeclarationInspection") @PathVariable("provider")
          AuthProvider provider,
      Authentication authentication) {
    log.debug("Linking account with provider: {}", provider);
    var userId = authenticatedUserResolver.requireUserId(authentication);
    log.debug("Logged user ID: {}", userId);
    var token = oAuth2ContextService.generateAccessToken(userId, OAuth2Mode.LINK);
    oAuth2ContextCookieService.writeTokenToResponseSetCookieHeader(token);
    return ResponseEntity.noContent().build();
  }

  // todo: add password reset flow - send email with token, validate token, set new password
}
