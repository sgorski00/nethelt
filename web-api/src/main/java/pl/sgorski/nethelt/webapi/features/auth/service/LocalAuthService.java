package pl.sgorski.nethelt.webapi.features.auth.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.UserAlreadyExistsException;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

  private final AuthMapper authMapper;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public User registerUser(RegisterUserCommand command) {
    if (userService.isUserPresent(command.email())) {
      throw new UserAlreadyExistsException();
    }

    var user = authMapper.toEntity(command);
    user.setPasswordHash(hashPassword(command.newPassword()));
    return userService.save(user);
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
    if (user.getPasswordHash() != null) {
      throw new IllegalStateException(
          "User already has a password. If you want to change it, use change password then.");
    }

    user.setPasswordHash(hashPassword(rawPassword));
    saveAndRevokeTokens(user);
  }

  @Transactional
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    var user = userService.getUser(userId);
    if (user.getPasswordHash() == null) {
      throw new IllegalStateException("User doesn't have local password yet.");
    }

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid current password.");
    }

    user.setPasswordHash(hashPassword(newPassword));
    saveAndRevokeTokens(user);
  }

  private String hashPassword(String rawPassword) {
    return Objects.requireNonNull(passwordEncoder.encode(rawPassword), "Password encoding failed");
  }

  private void saveAndRevokeTokens(User user) {
    userService.save(user);
    refreshTokenService.revokeAllUserTokens(user.getId());
  }
}
