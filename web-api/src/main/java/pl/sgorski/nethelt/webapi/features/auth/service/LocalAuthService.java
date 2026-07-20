package pl.sgorski.nethelt.webapi.features.auth.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.auth.UserAlreadyExistsException;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public User registerUser(RegisterUserCommand command) {
    if (userService.isUserPresent(command.email())) {
      throw new UserAlreadyExistsException();
    }

    var hashedPassword = hashPassword(command.newPassword());
    var user = new User(command.email(), hashedPassword);
    return userService.register(user);
  }

  public User login(LoginUserCommand command) {
    var user = getAuthenticatedUser(command);
    return userService.getUser(user.getId());
  }

  private User getAuthenticatedUser(LoginUserCommand command) {
    var auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.email(), command.password()));
    var principal = Objects.requireNonNull(auth.getPrincipal(), "Authentication failed");
    return (User) principal;
  }

  @Transactional
  public void setLocalPassword(Long userId, String rawPassword) {
    var user = userService.getUser(userId);
    if (user.isLocal()) {
      throw new IllegalStateException(
          "User already has a password. If you want to change it, use change password then.");
    }

    setPasswordAndRevokeTokens(user, rawPassword);
  }

  @Transactional
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    var user = userService.getUser(userId);
    if (!user.isLocal()) {
      throw new IllegalStateException("User doesn't have local password yet.");
    }

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new IllegalArgumentException("Invalid current password.");
    }

    setPasswordAndRevokeTokens(user, newPassword);
  }

  @Transactional
  public void resetPassword(User user, String newPassword) {
    if (!user.isLocal()) {
      throw new IllegalStateException("User doesn't have local password yet.");
    }

    setPasswordAndRevokeTokens(user, newPassword);
  }

  private void setPasswordAndRevokeTokens(User user, String rawPassword) {
    var hashedPassword = hashPassword(rawPassword);
    user.setPassword(hashedPassword);
    refreshTokenService.revokeAllUserTokens(user.getId());
  }

  private String hashPassword(String rawPassword) {
    return Objects.requireNonNull(passwordEncoder.encode(rawPassword), "Password encoding failed");
  }
}
