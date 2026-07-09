package pl.sgorski.nethelt.webapi.features.user.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;

public class UserTests {

  @Test
  void setProfile_shouldMapBidirectional() {
    var user = new User();
    var profile = new Profile();

    user.setProfile(profile);

    assertSame(user, profile.getUser());
    assertSame(profile, user.getProfile());
  }

  @Test
  void hasPasswordSet_shouldReturnTrue_whenPasswordHashIsNotNull() {
    var user = new User();
    user.setPasswordHash("hashed_password");

    var result = user.hasPasswordSet();

    assertTrue(result);
  }

  @Test
  void hasPasswordSet_shouldReturnFalse_whenPasswordHashIsNull() {
    var user = new User();
    user.setPasswordHash(null);

    var result = user.hasPasswordSet();

    assertFalse(result);
  }

  @Test
  void hasPasswordSet_shouldReturnFalse_whenPasswordHashIsBlank() {
    var user = new User();
    user.setPasswordHash("  ");

    var result = user.hasPasswordSet();

    assertFalse(result);
  }

  @Test
  void addIdentity_shouldAddIdentityToUserAndSetUserInIdentity() {
    var user = new User();
    var identity = new UserIdentity();
    identity.setProvider(AuthProvider.GITHUB);

    user.addIdentity(identity);

    assertTrue(user.getIdentities().contains(identity));
    assertSame(user, identity.getUser());
  }

  @Test
  void addIdentity_shouldThrowException_whenIdentityWithSameProviderExists() {
    var user = new User();
    var identity1 = new UserIdentity();
    identity1.setProvider(AuthProvider.GITHUB);
    user.addIdentity(identity1);

    var identity2 = new UserIdentity();
    identity2.setProvider(AuthProvider.GITHUB);

    assertThrows(IllegalStateException.class, () -> user.addIdentity(identity2));
  }
}
