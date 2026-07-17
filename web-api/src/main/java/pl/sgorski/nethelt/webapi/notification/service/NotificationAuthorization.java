package pl.sgorski.nethelt.webapi.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@Component
@RequiredArgsConstructor
public class NotificationAuthorization {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final NotificationRepository notificationRepository;

  public boolean isOwner(Authentication authentication, Long notificationId) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    return notificationRepository.existsByIdAndUserId(notificationId, userId);
  }
}
