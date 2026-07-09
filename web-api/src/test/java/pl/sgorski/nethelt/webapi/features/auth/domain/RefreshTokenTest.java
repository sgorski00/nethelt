package pl.sgorski.nethelt.webapi.features.auth.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

public class RefreshTokenTest {

  @Test
  void constructor_shouldCreateRefreshToken() {
    var user = new User();
    user.setId(1L);
    var expiresAt = Instant.now().plusSeconds(3600);

    var refreshToken = new RefreshToken(user, expiresAt);

    assertTrue(refreshToken.isValid());
    assertFalse(refreshToken.getToken().isBlank());
    assertEquals(user, refreshToken.getUser());
  }

  @Test
  void constructor_shouldGenerateUniqueTokens() {
    var expiresAt = Instant.now().plusSeconds(3600);

    var token1 = new RefreshToken(new User(), expiresAt);
    var token2 = new RefreshToken(new User(), expiresAt);

    assertNotEquals(token1.getToken(), token2.getToken());
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(new User(), expiresAt);

    assertTrue(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsRevoked() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(new User(), expiresAt);
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpired() {
    var expiresAt = Instant.now().minusSeconds(1);
    var refreshToken = new RefreshToken(new User(), expiresAt);

    assertFalse(refreshToken.isValid());
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsExpiredAndRevoked() {
    var expiresAt = Instant.now().minusSeconds(1);
    var refreshToken = new RefreshToken(new User(), expiresAt);
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void revoke_shouldMakeTokenInvalid() {
    var expiresAt = Instant.now().plusSeconds(3600);
    var refreshToken = new RefreshToken(new User(), expiresAt);

    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }

  @Test
  void revoke_shouldBeIdempotent() {
    var refreshToken = new RefreshToken(new User(), Instant.now().plusSeconds(3600));

    refreshToken.revoke();
    refreshToken.revoke();

    assertFalse(refreshToken.isValid());
  }
}
