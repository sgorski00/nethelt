package pl.sgorski.nethelt.webapi.features.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.sgorski.nethelt.webapi.exception.domain.UserAlreadyExistsException;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
public class LocalAuthServiceTest {

  private static final String EMAIL = "john.doe@example.com";
  private static final String PASSWORD = "password123";

  @Mock private UserService userService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private RefreshTokenService refreshTokenService;

  @Captor ArgumentCaptor<User> userCaptor;

  private LocalAuthService localAuthService;

  @BeforeEach
  void setUp() {
    var authMapper = Mappers.getMapper(AuthMapper.class);
    localAuthService =
        new LocalAuthService(
            authMapper, userService, passwordEncoder, authenticationManager, refreshTokenService);
  }

  @Test
  void registerUser_shouldRegisterUserSuccessfullyWithHashedPassword() {
    var command = new RegisterUserCommand(EMAIL, PASSWORD, PASSWORD);
    var hashedPassword = "hashed-password";
    when(passwordEncoder.encode(PASSWORD)).thenReturn(hashedPassword);
    when(userService.isUserPresent(EMAIL)).thenReturn(false);

    localAuthService.registerUser(command);
    verify(passwordEncoder).encode(PASSWORD);
    verify(userService).save(userCaptor.capture());
    var saved = userCaptor.getValue();

    assertEquals(EMAIL, saved.getEmail());
    assertEquals(hashedPassword, saved.getPasswordHash());
    assertEquals(Role.USER, saved.getRole());
  }

  @Test
  void registerUser_shouldThrow_whenUserIsAlreadyPresent() {
    var command = new RegisterUserCommand(EMAIL, PASSWORD, PASSWORD);
    when(userService.isUserPresent(EMAIL)).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> localAuthService.registerUser(command));
    verify(userService, never()).save(any());
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void login_shouldReturnAuthenticatedUser() {
    var user = createUser();
    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    var command = new LoginUserCommand(EMAIL, PASSWORD);
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(userService.getUser(1L)).thenReturn(user);

    var result = localAuthService.login(command);

    assertEquals(user, result);
    verify(authenticationManager).authenticate(any());
  }

  @Test
  void setPassword_shouldSetPasswordAndRevokeAllTokens() {
    var user = createUser();
    var hashedPassword = "hashed-password";
    when(userService.getUser(1L)).thenReturn(user);
    when(passwordEncoder.encode(PASSWORD)).thenReturn(hashedPassword);

    localAuthService.setLocalPassword(1L, PASSWORD);

    assertEquals(hashedPassword, user.getPasswordHash());
    verify(userService, times(1)).save(user);
    verify(passwordEncoder, times(1)).encode(PASSWORD);
    verify(refreshTokenService, times(1)).revokeAllUserTokens(user.getId());
  }

  @Test
  void setPassword_shouldThrow_whenPasswordIsAlreadySet() {
    var user = createUser();
    user.setPasswordHash("hashed-old-password");
    when(userService.getUser(1L)).thenReturn(user);

    assertThrows(
        IllegalStateException.class, () -> localAuthService.setLocalPassword(1L, PASSWORD));

    verify(passwordEncoder, never()).encode(PASSWORD);
    verify(userService, never()).save(any());
    verify(refreshTokenService, never()).revokeAllUserTokens(anyLong());
  }

  @Test
  void changePassword_shouldChangePasswordAndRevokeAllTokens() {
    var newPassword = "newPassword123";
    var hashedNewPassword = "hashed-new-password";
    var hashedOldPassword = "hashed-old-password";
    var user = createUser();
    user.setPasswordHash(hashedOldPassword);
    when(userService.getUser(1L)).thenReturn(user);
    when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(true);
    when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);

    localAuthService.changePassword(1L, PASSWORD, newPassword);

    assertEquals(hashedNewPassword, user.getPasswordHash());
    verify(userService, times(1)).save(user);
    verify(passwordEncoder, times(1)).matches(PASSWORD, hashedOldPassword);
    verify(passwordEncoder, times(1)).encode(newPassword);
    verify(refreshTokenService, times(1)).revokeAllUserTokens(user.getId());
  }

  @Test
  void changePassword_shouldThrow_whenPasswordIsNotSetYet() {
    var newPassword = "newPassword123";
    var user = createUser();
    when(userService.getUser(1L)).thenReturn(user);

    assertThrows(
        IllegalStateException.class,
        () -> localAuthService.changePassword(1L, PASSWORD, newPassword));

    verify(passwordEncoder, never()).encode(PASSWORD);
    verify(userService, never()).save(any());
    verify(refreshTokenService, never()).revokeAllUserTokens(anyLong());
  }

  @Test
  void changePassword_shouldThrow_whenOldPasswordDoesNotMatch() {
    var newPassword = "newPassword123";
    var user = createUser();
    user.setPasswordHash("hashed-old-password");
    when(userService.getUser(1L)).thenReturn(user);
    when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> localAuthService.changePassword(1L, PASSWORD, newPassword));

    verify(passwordEncoder, never()).encode(PASSWORD);
    verify(userService, never()).save(any());
    verify(refreshTokenService, never()).revokeAllUserTokens(anyLong());
  }

  private User createUser() {
    var user = new User();
    user.setId(1L);
    return user;
  }
}
