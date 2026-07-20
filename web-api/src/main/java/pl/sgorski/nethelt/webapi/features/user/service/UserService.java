package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.user.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.AccountCreatedEvent;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public User register(User user) {
    var saved = userRepository.save(user);
    eventPublisher.publishEvent(new AccountCreatedEvent(saved.getId()));
    return saved;
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

  public User getUserWithNotificationPreferences(Long id) {
    return userRepository
        .findWithNotificationPreferencesByIdAndDeletedAtIsNull(id)
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
