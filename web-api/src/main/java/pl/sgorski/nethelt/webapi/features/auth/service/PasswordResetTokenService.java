package pl.sgorski.nethelt.webapi.features.auth.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.auth.PasswordResetTokenNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.user.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.PasswordResetToken;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.PasswordResetRequestEvent;
import pl.sgorski.nethelt.webapi.features.auth.repository.PasswordResetTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

  private final AuthProperties authProperties;
  private final UserService userService;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void generate(String email) {
    try {
      var user = userService.getUser(email);
      if (user.isLocal()) {
        var expiration = authProperties.passwordResetTokenExpiration();
        var token = new PasswordResetToken(user, Instant.now().plus(expiration));
        var saved = passwordResetTokenRepository.save(token);
        sendPasswordResetLink(saved.getToken(), user.getId());
      }
    } catch (UserNotFoundException ignored) {
    }
  }

  @Transactional
  public User consume(String token) {
    var resetToken =
        passwordResetTokenRepository
            .findWithUserByToken(token)
            .filter(PasswordResetToken::isValid)
            .orElseThrow(PasswordResetTokenNotFoundException::new);
    resetToken.revoke();
    return resetToken.getUser();
  }

  private void sendPasswordResetLink(String token, Long userId) {
    var redirectUrl = authProperties.passwordResetConfirmUrl() + "?token=" + token;
    eventPublisher.publishEvent(new PasswordResetRequestEvent(userId, redirectUrl));
  }
}
