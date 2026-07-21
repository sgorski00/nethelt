package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.PasswordResetConfirmRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.PasswordResetRequest;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.auth.service.PasswordResetTokenService;

@RestController
@RequestMapping(value = "/auth/password-reset", version = "1")
@RequiredArgsConstructor
public final class PasswordResetController {

  private final PasswordResetTokenService passwordResetTokenService;
  private final LocalAuthService localAuthService;

  @PostMapping("/request")
  public ResponseEntity<Void> requestPasswordReset(
      @RequestBody @Valid PasswordResetRequest request) {
    passwordResetTokenService.generate(request.email());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/confirm")
  public ResponseEntity<Void> confirmPasswordReset(
      @RequestParam("token") String token,
      @RequestBody @Valid PasswordResetConfirmRequest request) {
    var user = passwordResetTokenService.consume(token);
    localAuthService.resetPassword(user, request.newPassword());
    return ResponseEntity.noContent().build();
  }
}
