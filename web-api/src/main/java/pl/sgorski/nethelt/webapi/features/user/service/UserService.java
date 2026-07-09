package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public User save(User user) {
    return userRepository.save(user);
  }

  public User getUser(String email) {
    return userRepository
        .findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
  }

  public User getUser(Long id) {
    return userRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
  }

  public User getUserWithProfileAndIdentities(Long id) {
    return userRepository
        .findWithIdentitiesAndProfileByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
  }

  public boolean isUserPresent(String email) {
    return userRepository.existsByEmailAndDeletedAtIsNull(email);
  }

  @Transactional
  public void removeOAuth2AccountLink(Long userId, AuthProvider authProvider) {
    var user = getUser(userId);
    user.removeIdentityByProvider(authProvider);
  }
}
