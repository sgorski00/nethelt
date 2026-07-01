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

  /** Validate user's login request and returns user if credentials are correct */
  public User login(LoginUserCommand command) {
    var auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.email(), command.password()));
    var principal = Objects.requireNonNull(auth.getPrincipal(), "Authentication failed");
    var user = (User) principal;
    return userService.getUser(user.getId());
  }

  /**
   * Method that allows oauth2 users to create local password and login with email and password.
   * Revokes all existing refresh tokens to force re-authentication.
   */
  @Transactional
  public void setLocalPassword(User user, String rawPassword) {
    if (user.getPasswordHash() != null) {
      throw new IllegalStateException(
          "User already has a password. If you want to change it, use change password then.");
    }
    user.setPasswordHash(hashPassword(rawPassword));
    userService.save(user);
    refreshTokenService.revokeAllUserTokens(user.getId());
  }

  @Transactional
  public void changePassword(User user, String oldPassword, String newPassword) {
    if (user.getPasswordHash() == null) {
      throw new IllegalStateException("User doesn't have local password yet.");
    }

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid current password.");
    }

    user.setPasswordHash(hashPassword(newPassword));
    userService.save(user);
    refreshTokenService.revokeAllUserTokens(user.getId());
  }

  private String hashPassword(String rawPassword) {
    return Objects.requireNonNull(passwordEncoder.encode(rawPassword), "Password encoding failed");
  }
}
