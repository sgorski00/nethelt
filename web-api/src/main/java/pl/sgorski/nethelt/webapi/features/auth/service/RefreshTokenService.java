package pl.sgorski.nethelt.webapi.features.auth.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;
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
    var tokenStr = UUID.randomUUID().toString();
    var token = new RefreshToken(tokenStr, user, refreshTokenProperties.expirationTimeInMs());
    return refreshTokenRepository.save(token);
  }

  /**
   * Validates and retrieves user from refresh token. Throws exception if token is invalid, expired,
   * or revoked.
   *
   * @param tokenStr the refresh token string
   * @return the user associated with the token
   * @throws NotFoundException if token not found or invalid
   */
  public User validateAndGetUser(String tokenStr) {
    var token =
        refreshTokenRepository
            .findWithUserByToken(tokenStr)
            .orElseThrow(() -> new NotFoundException("Invalid or expired refresh token"));

    if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
      throw new NotFoundException("Invalid or expired refresh token");
    }

    return token.getUser();
  }

  @Transactional
  public void revokeToken(String tokenStr) {
    refreshTokenRepository
        .findByToken(tokenStr)
        .ifPresent(
            t -> {
              t.setRevoked(true);
              refreshTokenRepository.save(t);
            });
  }

  @Transactional
  public void revokeAllUserTokens(Long userId) {
    refreshTokenRepository.revokeAllUserTokens(userId);
  }

  public long getExpirationSecond() {
    return refreshTokenProperties.expirationTimeInMs() / 1000;
  }

  @Transactional
  public void deletedInvalidTokens() {
    refreshTokenRepository.deleteAllByExpiresAtBeforeOrIsRevokedTrue(Instant.now());
  }
}
