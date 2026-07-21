package pl.sgorski.nethelt.webapi.features.auth.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.auth.RefreshTokenNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.RefreshTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final AuthProperties authProperties;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshToken generateRefreshToken(User user) {
    var expiration = authProperties.refreshTokenExpiration();
    var token = new RefreshToken(user, Instant.now().plus(expiration));
    return refreshTokenRepository.save(token);
  }

  public User validateAndGetUser(String token) {
    return refreshTokenRepository
        .findWithUserByToken(token)
        .filter(RefreshToken::isValid)
        .map(RefreshToken::getUser)
        .orElseThrow(RefreshTokenNotFoundException::new);
  }

  @Transactional
  public void revokeToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(RefreshToken::revoke);
  }

  @Transactional
  public void revokeAllUserTokens(Long userId) {
    refreshTokenRepository.revokeAllUserTokens(userId);
  }

  @Transactional
  public void deleteInvalidTokens() {
    refreshTokenRepository.deleteAllByExpiresAtBeforeOrIsRevokedTrue(Instant.now());
  }
}
