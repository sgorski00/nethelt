package pl.sgorski.nethelt.webapi.features.auth.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pl.sgorski.nethelt.webapi.utils.TestUserFactory.createLocalUser;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class RefreshTokenTest {

  @Test
  void constructor_shouldCreateRefreshToken() {
    var user = TestUserFactory.createLocalUser();
    var expiresAt = Instant.now().plusSeconds(3600);

    var refreshToken = new RefreshToken(user, expiresAt);

    assertTrue(refreshToken.isValid());
    assertFalse(refreshToken.getToken().isBlank());
    assertEquals(user, refreshToken.getUser());
  }

  @Test
  void constructor_shouldGenerateUniqueTokens() {
    var expiresAt = Instant.now().plusSeconds(3600);

    var token1 = new RefreshToken(createLocalUser("user1@example.com"), expiresAt);
    var token2 = new RefreshToken(createLocalUser("user2@example.com"), expiresAt);

    assertNotEquals(token1.getToken(), token2.getToken());
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);

    assertTrue(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsRevoked() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpired() {
    var expiresAt = Instant.now().minusSeconds(1);
    var refreshToken = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);

    assertFalse(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpiredAndRevoked() {
    var expiresAt = Instant.now().minusSeconds(1);
    var refreshToken = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void revoke_shouldMakeTokenInvalid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(TestUserFactory.createLocalUser(), expiresAt);

    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void revoke_shouldBeIdempotent() {
    var refreshToken =
        new RefreshToken(TestUserFactory.createLocalUser(), Instant.now().plusSeconds(3600));

    refreshToken.revoke();
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }
}
