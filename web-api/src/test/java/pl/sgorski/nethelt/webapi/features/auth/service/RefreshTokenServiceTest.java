package pl.sgorski.nethelt.webapi.features.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.RefreshTokenNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.RefreshTokenRepository;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

  @Mock private AuthProperties authProperties;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokenService refreshTokenService;

  @Test
  void generateRefreshToken_shouldReturnValidToken() {
    var user = TestUserFactory.createLocalUser();
    when(authProperties.refreshTokenExpiration()).thenReturn(Duration.ofMinutes(60));
    when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    var token = refreshTokenService.generateRefreshToken(user);

    assertTrue(token.isValid());
    assertEquals(user, token.getUser());
  }

  @Test
  void validateAndGetUser_shouldReturnUserForValidToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new RefreshToken(user, expiresAt);
    when(refreshTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    var retrieved = refreshTokenService.validateAndGetUser(token.getToken());

    assertEquals(user, retrieved);
  }

  @Test
  void validateAndGetUser_shouldThrow_whenTokenNotFound() {
    var expiresAt = getExpiresAt();
    var token = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);
    when(refreshTokenRepository.findWithUserByToken(token.getToken())).thenReturn(Optional.empty());

    assertThrows(
        RefreshTokenNotFoundException.class,
        () -> refreshTokenService.validateAndGetUser(token.getToken()));
  }

  @Test
  void validateAndGetUser_shouldThrow_whenTokenIsNotValid() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new RefreshToken(user, expiresAt);
    token.revoke();
    when(refreshTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    assertThrows(
        RefreshTokenNotFoundException.class,
        () -> refreshTokenService.validateAndGetUser(token.getToken()));
  }

  @Test
  void revokeToken_shouldRevokeToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new RefreshToken(user, expiresAt);
    when(refreshTokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));
    when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    refreshTokenService.revokeToken(token.getToken());

    assertFalse(token.isValid());
  }

  @Test
  void revokeToken_shouldSkip_whenNotPresent() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new RefreshToken(user, expiresAt);
    when(refreshTokenRepository.findByToken(token.getToken())).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> refreshTokenService.revokeToken(token.getToken()));
  }

  @Test
  void revokeAllUserTokens_shouldRevokeAllTokens() {
    var userId = 1L;

    assertDoesNotThrow(() -> refreshTokenService.revokeAllUserTokens(userId));

    verify(refreshTokenRepository, times(1)).revokeAllUserTokens(userId);
  }

  @Test
  void deleteInvalidTokens_shouldDeleteInvalidTokens() {
    assertDoesNotThrow(() -> refreshTokenService.deleteInvalidTokens());

    verify(refreshTokenRepository, times(1)).deleteAllByExpiresAtBeforeOrIsRevokedTrue(any());
  }

  private Instant getExpiresAt() {
    return Instant.now().plusMillis(60000L);
  }
}
