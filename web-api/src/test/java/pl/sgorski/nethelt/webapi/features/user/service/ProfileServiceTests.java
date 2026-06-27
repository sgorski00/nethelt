package pl.sgorski.nethelt.webapi.features.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileUpdateCommand;
import pl.sgorski.nethelt.webapi.features.user.mapper.ProfileMapper;
import pl.sgorski.nethelt.webapi.features.user.repository.ProfileRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTests {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    private ProfileService profileService;

    private final ProfileMapper profileMapper = Mappers.getMapper(ProfileMapper.class);

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
        this.profileService = new ProfileService(profileRepository, profileMapper, userService);
        this.profile = new Profile();
        this.user = new User();
        user.setId(userId);
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

        verify(userService).save(user);
    }

    @Test
    void createProfile_shouldNotSave_whenUserNotFound() {
        var command = new ProfileCreateCommand(userId, username, firstName, lastName, birthDate, bio);
        when(userService.getUser(userId)).thenThrow(new UserNotFoundException(username));

        assertThrows(UserNotFoundException.class, () -> profileService.createProfile(command));
        verify(userService, never()).save(any());
    }

    @Test
    void createProfile_shouldNotSave_whenUProfileAlreadyExists() {
        var command = new ProfileCreateCommand(userId, username, firstName, lastName, birthDate, bio);
        user.setProfile(profile);
        when(userService.getUser(userId)).thenReturn(user);
        
        assertThrows(ProfileAlreadyExistsException.class, () -> profileService.createProfile(command));
        verify(userService, never()).save(any());
    }

    @Test
    void updateProfile_shouldMapToExistingProfile() {
        var command = new ProfileUpdateCommand(userId, firstName, lastName, birthDate, bio);
        user.setProfile(profile);
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
