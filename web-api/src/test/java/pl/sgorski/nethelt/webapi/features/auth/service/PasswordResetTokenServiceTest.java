package pl.sgorski.nethelt.webapi.features.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.sgorski.nethelt.webapi.exception.domain.auth.PasswordResetTokenNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.user.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.PasswordResetToken;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.PasswordResetRequestEvent;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.repository.PasswordResetTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenServiceTest {

  @Mock private AuthProperties authProperties;
  @Mock private UserService userService;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private PasswordResetTokenService passwordResetTokenService;

  @Test
  void generate() {
    var user = TestUserFactory.createLocalUser();
    when(userService.getUser("john.doe@example.com")).thenReturn(user);
    when(authProperties.passwordResetTokenExpiration()).thenReturn(Duration.ofMinutes(60));
    when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    passwordResetTokenService.generate("john.doe@example.com");

    var captor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(passwordResetTokenRepository).save(captor.capture());
    verify(eventPublisher).publishEvent(any(PasswordResetRequestEvent.class));
    var captured = captor.getValue();
    assertTrue(captured.isValid());
    assertEquals(user, captured.getUser());
  }

  @Test
  void generate_shouldNotGenerate_whenUserIsNotLocal() {
    var user = TestUserFactory.createOAuth2User(AuthProvider.GITHUB);
    when(userService.getUser("john.doe@example.com")).thenReturn(user);

    assertDoesNotThrow(() -> passwordResetTokenService.generate("john.doe@example.com"));
    verify(passwordResetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any(PasswordResetRequestEvent.class));
  }

  @Test
  void generate_shouldNotGenerateAndIgnoreException_whenUserNotFound() {
    when(userService.getUser("john.doe@example.com")).thenThrow(UserNotFoundException.class);

    assertDoesNotThrow(() -> passwordResetTokenService.generate("john.doe@example.com"));
    verify(passwordResetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any(PasswordResetRequestEvent.class));
  }

  @Test
  void consume_shouldRevokeAndReturnUser_whenValidToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    assertTrue(token.isValid());
    var retrieved = passwordResetTokenService.consume(token.getToken());

    assertEquals(user, retrieved);
    assertFalse(token.isValid());
  }

  @Test
  void consume_shouldThrow_whenTokenNotFound() {
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.empty());

    assertThrows(
        PasswordResetTokenNotFoundException.class,
        () -> passwordResetTokenService.consume(token.getToken()));
  }

  @Test
  void consume_shouldThrow_whenTokenIsNotValid() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = getExpiresAt();
    var token = new PasswordResetToken(user, expiresAt);
    token.revoke();
    when(passwordResetTokenRepository.findWithUserByToken(token.getToken()))
        .thenReturn(Optional.of(token));

    assertThrows(
        PasswordResetTokenNotFoundException.class,
        () -> passwordResetTokenService.consume(token.getToken()));
  }

  private Instant getExpiresAt() {
    return Instant.now().plusMillis(60000L);
  }
}
