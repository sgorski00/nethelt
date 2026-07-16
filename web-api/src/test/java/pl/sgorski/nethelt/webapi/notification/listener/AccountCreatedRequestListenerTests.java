package pl.sgorski.nethelt.webapi.notification.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationPreferencesNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.AccountCreatedEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class AccountCreatedRequestListenerTests {

  @Mock private NotificationPreferencesService notificationPreferencesService;
  @Mock private NotificationService notificationService;
  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private AccountCreatedRequestListener accountCreatedRequestListener;

  @Test
  void handle_shouldSendNotificationOnAccountCreatedEvent() {
    var user = TestUserFactory.createLocalUser();
    var notification = new Notification(user, "test", "test");
    when(notificationPreferencesService.getPreferences(1L))
        .thenReturn(user.getNotificationPreferences());
    when(notificationService.create(any())).thenReturn(notification);

    accountCreatedRequestListener.handle(new AccountCreatedEvent(1L));

    verify(notificationService)
        .create(
            argThat(
                command ->
                    command.userId().equals(1L)
                        && !command.title().isBlank()
                        && !command.content().isBlank()));
    verify(notificationDeliveryService)
        .send(notification, user.getNotificationPreferences().getEnabledChannels());
  }

  @Test
  void handle_shouldNotSend_whenErrorOccurs() {
    when(notificationPreferencesService.getPreferences(1L))
        .thenThrow(NotificationPreferencesNotFoundException.class);

    accountCreatedRequestListener.handle(new AccountCreatedEvent(1L));

    verify(notificationDeliveryService, never()).send(any(), any());
    verify(notificationService, never()).create(any());
  }
}
