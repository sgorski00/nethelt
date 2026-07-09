package pl.sgorski.nethelt.webapi.features.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  void save_shouldSaveUser() {
    var user = TestUserFactory.createLocalUser();

    userService.save(user);

    verify(userRepository, times(1)).save(user);
  }

  @Test
  void getUser_shouldFindUserByEmail() {
    var user = TestUserFactory.createLocalUser();
    var email = user.getEmail();
    when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));

    var result = userService.getUser(email);

    assertSame(user, result);
  }

  @Test
  void getUser_shouldThrow_whenUserNotFoundByEmail() {
    var email = "user@example.com";
    when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUser(email));
  }

  @Test
  void getUser_shouldFindUserById() {
    var user = TestUserFactory.createLocalUser();
    var id = 1L;
    when(userRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

    var result = userService.getUser(id);

    assertSame(user, result);
  }

  @Test
  void getUser_shouldThrow_whenUserNotFoundById() {
    var id = 1L;
    when(userRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUser(id));
  }

  @Test
  void getUserWithProfileAndIdentities_shouldFindUser() {
    var user = TestUserFactory.createLocalUser();
    var id = 1L;
    when(userRepository.findWithIdentitiesAndProfileByIdAndDeletedAtIsNull(id))
        .thenReturn(Optional.of(user));

    var result = userService.getUserWithProfileAndIdentities(id);

    assertSame(user, result);
  }

  @Test
  void getUserWithProfileAndIdentities_shouldThrow_whenUserNotFoundById() {
    var id = 1L;
    when(userRepository.findWithIdentitiesAndProfileByIdAndDeletedAtIsNull(id))
        .thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> userService.getUserWithProfileAndIdentities(id));
  }

  @Test
  void isUserPresent_shouldReturnTrue_whenUserExists() {
    var email = "user@example.com";
    when(userRepository.existsByEmailAndDeletedAtIsNull(email)).thenReturn(true);

    var result = userService.isUserPresent(email);

    assertTrue(result);
  }

  @Test
  void isUserPresent_shouldReturnFalse_whenUserDoesNotExists() {
    var email = "user@example.com";
    when(userRepository.existsByEmailAndDeletedAtIsNull(email)).thenReturn(false);

    var result = userService.isUserPresent(email);

    assertFalse(result);
  }

  @Test
  void removeOAuth2AccountLink_shouldRemove_whenUserFound() {
    var user = TestUserFactory.createLocalUser();
    user.addIdentity(AuthProvider.GITHUB, "test-provider-id-1");
    var id = 1L;
    when(userRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

    userService.removeOAuth2AccountLink(id, AuthProvider.GITHUB);

    assertTrue(user.getIdentities().isEmpty());
  }

  @Test
  void removeOAuth2AccountLink_shouldThrow_whenUserNotFound() {
    var id = 1L;
    when(userRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> userService.removeOAuth2AccountLink(id, AuthProvider.GITHUB));
  }
}
