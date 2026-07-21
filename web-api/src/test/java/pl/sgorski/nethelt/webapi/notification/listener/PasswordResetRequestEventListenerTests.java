package pl.sgorski.nethelt.webapi.notification.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.PasswordResetRequestEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class PasswordResetRequestEventListenerTests {

  @Mock private NotificationService notificationService;
  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private PasswordResetRequestEventListener passwordResetRequestEventListener;

  @Test
  void handle_shouldSendNotificationOnPasswordResetRequestEvent() {
    var user = TestUserFactory.createLocalUser();
    var notification = new Notification(user, "test", "test");
    when(notificationService.create(any())).thenReturn(notification);

    passwordResetRequestEventListener.handle(
        new PasswordResetRequestEvent(1L, "http://example.com/reset"));

    verify(notificationService)
        .create(
            argThat(
                command ->
                    command.userId().equals(1L)
                        && !command.title().isBlank()
                        && !command.content().isBlank()));
    verify(notificationDeliveryService).send(notification, Set.of(NotificationChannel.EMAIL));
  }

  @Test
  void handle_shouldNotSend_whenErrorOccurs() {
    doThrow(RuntimeException.class).when(notificationService).create(any());

    passwordResetRequestEventListener.handle(
        new PasswordResetRequestEvent(1L, "http://example.com/reset"));

    verify(notificationDeliveryService, never()).send(any(), any());
  }
}
