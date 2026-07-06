package pl.sgorski.nethelt.webapi.features.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordChangeRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordSetRequest;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@RestController
@RequestMapping(path = "/profile/password", version = "1")
@RequiredArgsConstructor
public final class UserPasswordController {

  private final UserService userService;
  private final LocalAuthService localAuthService;
  private final AuthenticatedUserResolver authenticatedUserResolver;

  @PutMapping
  public ResponseEntity<Void> setLocalPassword(
      @RequestBody @Valid PasswordSetRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUser(userId);
    localAuthService.setLocalPassword(user, request.newPassword());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid PasswordChangeRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUser(userId);
    localAuthService.changePassword(user, request.oldPassword(), request.newPassword());
    return ResponseEntity.noContent().build();
  }

  // todo: add password reset flow - send email with token, validate token, set new password
}
