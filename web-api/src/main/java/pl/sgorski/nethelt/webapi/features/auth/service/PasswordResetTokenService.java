package pl.sgorski.nethelt.webapi.features.auth.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.auth.PasswordResetTokenNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.PasswordResetToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.PasswordResetTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

  private final AuthProperties authProperties;
  private final PasswordResetTokenRepository passwordResetTokenRepository;

  @Transactional
  public PasswordResetToken generatePasswordResetToken(User user) {
    var expiration = authProperties.passwordResetTokenExpiration();
    var token = new PasswordResetToken(user, Instant.now().plus(expiration));
    return passwordResetTokenRepository.save(token);
  }

  public User validateAndGetUser(String token) {
    return passwordResetTokenRepository
        .findWithUserByToken(token)
        .filter(PasswordResetToken::isValid)
        .map(PasswordResetToken::getUser)
        .orElseThrow(PasswordResetTokenNotFoundException::new);
  }

  @Transactional
  public void revokeToken(String token) {
    passwordResetTokenRepository.findByToken(token).ifPresent(PasswordResetToken::revoke);
  }
}
