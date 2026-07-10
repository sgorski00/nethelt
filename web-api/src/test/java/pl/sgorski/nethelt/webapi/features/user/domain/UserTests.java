package pl.sgorski.nethelt.webapi.features.user.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileOperationNotAllowedException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class UserTests {

  @Test
  void addProfile_shouldMapBidirectional() {
    var user = TestUserFactory.createLocalUser();
    var profile = new Profile();

    user.addProfile(profile);

    assertSame(user, profile.getUser());
    assertSame(profile, user.getProfile());
  }

  @Test
  void addProfile_shouldThrow_whenProfileAlreadyExists() {
    var user = TestUserFactory.createLocalUser();
    var profile = new Profile();

    user.addProfile(profile);

    assertThrows(ProfileAlreadyExistsException.class, () -> user.addProfile(new Profile()));
  }

  @Test
  void isLocal_shouldReturnTrue_whenPasswordHashIsNotNull() {
    var user = TestUserFactory.createLocalUser();

    var result = user.isLocal();

    assertTrue(result);
  }

  @Test
  void isLocal_shouldReturnFalse_whenPasswordHashIsNull() {
    var user = new User();

    var result = user.isLocal();

    assertFalse(result);
  }

  @Test
  void isLocal_shouldReturnFalse_whenPasswordHashIsBlank() {
    var user = TestUserFactory.createLocalUser();
    user.setPassword("  ");

    var result = user.isLocal();

    assertFalse(result);
  }

  @Test
  void addIdentity_shouldAddIdentityToUserAndSetUserInIdentity() {
    var user = TestUserFactory.createLocalUser();

    user.addIdentity(AuthProvider.GITHUB, "test-provider-id");
    assertEquals(1, user.getIdentities().size());

    var identity = user.getIdentities().iterator().next();
    assertEquals(user, identity.getUser());
  }

  @Test
  void addIdentity_shouldThrowException_whenIdentityWithSameProviderExists() {
    var user = TestUserFactory.createOAuth2User(AuthProvider.GITHUB);

    assertThrows(
        IllegalStateException.class,
        () -> user.addIdentity(AuthProvider.GITHUB, "test-provider-id-2"));
  }

  @Test
  void removeIdentityByProvider_shouldRemoveExistingIdentity_whenAnotherOneIsLinked() {
    var user = TestUserFactory.createOAuth2User(AuthProvider.GITHUB);
    user.addIdentity(AuthProvider.GOOGLE, "test-provider-id-2");

    assertEquals(2, user.getIdentities().size());
    user.removeIdentityByProvider(AuthProvider.GITHUB);
    assertEquals(1, user.getIdentities().size());

    assertEquals(AuthProvider.GOOGLE, user.getIdentities().iterator().next().getProvider());
  }

  @Test
  void removeIdentityByProvider_shouldRemoveExistingIdentity_whenAccountHasLocalPassword() {
    var user = TestUserFactory.createLocalUser();
    user.addIdentity(AuthProvider.GITHUB, "test-provider-id-1");

    assertEquals(1, user.getIdentities().size());
    user.removeIdentityByProvider(AuthProvider.GITHUB);
    assertEquals(0, user.getIdentities().size());
  }

  @Test
  void removeIdentityByProvider_shouldThrow_whenAccountIsNotLocalAndOnlyOneProviderLeft() {
    var user = TestUserFactory.createOAuth2User(AuthProvider.GITHUB);

    assertThrows(
        ProfileOperationNotAllowedException.class,
        () -> user.removeIdentityByProvider(AuthProvider.GITHUB));
  }

  @Test
  void constructor_shouldCreateLocalUserCorrectly() {
    var user = new User("john.doe@example.com", "hashed-password");

    assertEquals("john.doe@example.com", user.getEmail());
    assertEquals("hashed-password", user.getPassword());
    assertEquals(Role.USER, user.getRole());
  }

  @Test
  void constructor_shouldCreateOAuth2UserCorrectly() {
    var user = new User("john.doe@example.com", AuthProvider.GITHUB, "provider-id-1");

    assertEquals("john.doe@example.com", user.getEmail());
    assertNull(user.getPassword());
    assertEquals(Role.USER, user.getRole());
    assertEquals(1, user.getIdentities().size());
    var identity = user.getIdentities().iterator().next();
    assertEquals(AuthProvider.GITHUB, identity.getProvider());
    assertEquals("provider-id-1", identity.getProviderId());
  }

  @Test
  void getUsername_shouldReturnEmail() {
    var user = TestUserFactory.createLocalUser("john.doe@example.com");

    var username = user.getUsername();

    assertEquals("john.doe@example.com", username);
  }

  @Test
  void isEnabled_shouldReturnTrue_whenDeletedAtIsNull() {
    var user = TestUserFactory.createLocalUser();

    var isEnabled = user.isEnabled();

    assertTrue(isEnabled);
  }

  @Test
  void isEnabled_shouldReturnFalse_whenDeletedAtIsNotNull() {
    var user = TestUserFactory.createLocalUser();
    user.delete();

    var isEnabled = user.isEnabled();

    assertFalse(isEnabled);
  }

  @Test
  void delete_shouldSoftDeleteAccount() {
    var user = TestUserFactory.createLocalUser();

    user.delete();

    assertNotNull(user.getDeletedAt());
  }
}
