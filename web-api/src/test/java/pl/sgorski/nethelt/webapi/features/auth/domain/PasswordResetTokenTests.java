package pl.sgorski.nethelt.webapi.features.auth.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pl.sgorski.nethelt.webapi.utils.TestUserFactory.createLocalUser;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class PasswordResetTokenTests {

  @Test
  void constructor_shouldCreatePasswordResetToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = Instant.now().plusSeconds(3600);

    var passwordResetToken = new PasswordResetToken(user, expiresAt);

    assertTrue(passwordResetToken.isValid());
    assertFalse(passwordResetToken.getToken().isBlank());
    assertEquals(user, passwordResetToken.getUser());
  }

  @Test
  void constructor_shouldGenerateUniqueTokens() {
    var expiresAt = Instant.now().plusSeconds(3600);

    var token1 = new PasswordResetToken(createLocalUser("user1@example.com"), expiresAt);
    var token2 = new PasswordResetToken(createLocalUser("user2@example.com"), expiresAt);

    assertNotEquals(token1.getToken(), token2.getToken());
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var passwordResetToken = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);

    assertTrue(passwordResetToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsRevoked() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var passwordResetToken = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);
    passwordResetToken.revoke();

    assertFalse(passwordResetToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpired() {
    var expiresAt = Instant.now().minusSeconds(1);
    var passwordResetToken = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);

    assertFalse(passwordResetToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpiredAndRevoked() {
    var expiresAt = Instant.now().minusSeconds(1);
    var passwordResetToken = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);
    passwordResetToken.revoke();

    assertFalse(passwordResetToken.isValid());
  }

  @Test
  void revoke_shouldMakeTokenInvalid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var passwordResetToken = new PasswordResetToken(TestUserFactory.createLocalUser(), expiresAt);

    passwordResetToken.revoke();

    assertFalse(passwordResetToken.isValid());
  }

  @Test
  void revoke_shouldBeIdempotent() {
    var passwordResetToken =
        new PasswordResetToken(TestUserFactory.createLocalUser(), Instant.now().plusSeconds(3600));

    passwordResetToken.revoke();
    passwordResetToken.revoke();

    assertFalse(passwordResetToken.isValid());
  }
}
