package pl.sgorski.nethelt.webapi.notification.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@ExtendWith(MockitoExtension.class)
public class NotificationAuthorizationTests {

  @Mock private Authentication authentication;
  @Mock private AuthenticatedUserResolver authenticatedUserResolver;
  @Mock private NotificationRepository notificationRepository;
  @InjectMocks private NotificationAuthorization notificationAuthorization;

  @Test
  void isOwner_shouldReturnTrue_whenNotificationExistsForUser() {
    when(authenticatedUserResolver.requireUserId(any())).thenReturn(1L);
    when(notificationRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

    var result = notificationAuthorization.isOwner(authentication, 1L);

    assertTrue(result);
  }

  @Test
  void isOwner_shouldReturnFalse_whenNotificationExistsForUser() {
    when(authenticatedUserResolver.requireUserId(any())).thenReturn(1L);
    when(notificationRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

    var result = notificationAuthorization.isOwner(authentication, 1L);

    assertFalse(result);
  }
}
