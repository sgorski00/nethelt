package pl.sgorski.nethelt.webapi.features.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileUpdateCommand;
import pl.sgorski.nethelt.webapi.features.user.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTests {

  @Mock private ProfileRepository profileRepository;

  @Mock private UserService userService;

  @InjectMocks private ProfileService profileService;

  private final Long userId = 1L;
  private final String username = "test-user";
  private final String firstName = "John";
  private final String lastName = "Doe";
  private final LocalDate birthDate = LocalDate.of(1990, 1, 1);
  private final String bio = "This is a test bio.";
  private User user;
  private Profile profile;

  @BeforeEach
  void setUp() {
    this.profile = new Profile(username, firstName, lastName, birthDate, bio);
    this.user = new User("john.doe@example.com", "password123");
  }

  @Test
  void createProfile_shouldMapProfileAndAssignItToUser() {
    var command = new ProfileCreateCommand(userId, username, firstName, lastName, birthDate, bio);
    when(userService.getUser(userId)).thenReturn(user);

    var result = profileService.createProfile(command);

    assertEquals(username, result.getUsername());
    assertEquals(firstName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(birthDate, result.getBirthDate());
    assertEquals(bio, result.getBio());

    assertSame(user, result.getUser());
    assertSame(result, user.getProfile());
  }

  @Test
  void createProfile_shouldNotSave_whenUserNotFound() {
    var command = new ProfileCreateCommand(userId, username, firstName, lastName, birthDate, bio);
    when(userService.getUser(userId)).thenThrow(new UserNotFoundException(username));

    assertThrows(UserNotFoundException.class, () -> profileService.createProfile(command));
  }

  @Test
  void updateProfile_shouldMapToExistingProfile() {
    var command = new ProfileUpdateCommand(userId, firstName, lastName, birthDate, bio);
    user.addProfile(profile);
    when(profileRepository.findWithUserByUserId(userId)).thenReturn(Optional.ofNullable(profile));

    var result = profileService.updateProfile(command);

    assertEquals(firstName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(birthDate, result.getBirthDate());
    assertEquals(bio, result.getBio());

    assertSame(user, result.getUser());
    assertSame(result, user.getProfile());
  }

  @Test
  void updateProfile_shouldNotUpdate_whenProfileForUserNotFound() {
    var command = new ProfileUpdateCommand(userId, firstName, lastName, birthDate, bio);
    when(profileRepository.findWithUserByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(ProfileNotFoundException.class, () -> profileService.updateProfile(command));
  }
}
