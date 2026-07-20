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
import pl.sgorski.nethelt.webapi.exception.domain.auth.PasswordResetTokenNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.PasswordResetToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.PasswordResetTokenRepository;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenServiceTest {

  @Mock private AuthProperties authProperties;

  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

  @InjectMocks private PasswordResetTokenService passwordResetTokenService;

  @Test
  void generatePasswordResetToken_shouldReturnValidToken() {
    var user = TestUserFactory.createLocalUser();
    when(authProperties.passwordResetTokenExpiration()).thenReturn(Duration.ofMinutes(60));
    when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    var token = passwordResetTokenService.generatePasswordResetToken(user);

    assertTrue(token.isValid());
    assertEquals(user, token.getUser());
  }

  @Test
  void validateAndGetUser_shouldReturnUserForValidToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    var retrieved = passwordResetTokenService.validateAndGetUser(token.getToken());

    assertEquals(user, retrieved);
  }

  @Test
  void validateAndGetUser_shouldThrow_whenTokenNotFound() {
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.empty());

    assertThrows(
        PasswordResetTokenNotFoundException.class,
        () -> passwordResetTokenService.validateAndGetUser(token.getToken()));
  }

  @Test
  void validateAndGetUser_shouldThrow_whenTokenIsNotValid() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    token.revoke();
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    assertThrows(
        PasswordResetTokenNotFoundException.class,
        () -> passwordResetTokenService.validateAndGetUser(token.getToken()));
  }

  @Test
  void revokeToken_shouldRevokeToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    when(passwordResetTokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

    passwordResetTokenService.revokeToken(token.getToken());

    assertFalse(token.isValid());
  }

  @Test
  void revokeToken_shouldSkip_whenNotPresent() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    when(passwordResetTokenRepository.findByToken(token.getToken())).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> passwordResetTokenService.revokeToken(token.getToken()));
  }

  private Instant getExpiresAt() {
    return Instant.now().plusMillis(60000L);
  }
}
