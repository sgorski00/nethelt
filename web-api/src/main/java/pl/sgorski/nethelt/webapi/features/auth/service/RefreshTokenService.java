package pl.sgorski.nethelt.webapi.features.auth.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.RefreshTokenNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.RefreshTokenProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.RefreshTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenProperties refreshTokenProperties;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshToken generateRefreshToken(User user) {
    var expirationTimeInMs = refreshTokenProperties.expirationTimeInMs();
    var token = new RefreshToken(user, Instant.now().plusMillis(expirationTimeInMs));
    return refreshTokenRepository.save(token);
  }

  public User validateAndGetUser(String tokenStr) {
    var token =
        refreshTokenRepository
            .findWithUserByToken(tokenStr)
            .orElseThrow(RefreshTokenNotFoundException::new);

    if (!token.isValid()) {
      throw new RefreshTokenNotFoundException();
    }

    return token.getUser();
  }

  @Transactional
  public void revokeToken(String tokenStr) {
    refreshTokenRepository
        .findByToken(tokenStr)
        .ifPresent(
            t -> {
              t.revoke();
              refreshTokenRepository.save(t);
            });
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
