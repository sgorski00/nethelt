package pl.sgorski.nethelt.webapi.notification.service;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationDeliveryServiceTests {

  @Mock private NotificationSender notificationSender;
  private NotificationDeliveryService notificationService;

  @BeforeEach
  void setUp() {
    this.notificationService = new NotificationDeliveryService(List.of(notificationSender));
  }

  @Test
  void send_shouldCallSendersForSupportedChannels_whenChannelIsSupported() {
    var notification = TestNotificationFactory.createNotification();
    when(notificationSender.supports(NotificationChannel.EMAIL)).thenReturn(true);

    notificationService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(notificationSender).send(notification);
  }

  @Test
  void send_shouldNotCallSenders_whenChannelIsNotSupported() {
    var notification = TestNotificationFactory.createNotification();
    when(notificationSender.supports(NotificationChannel.EMAIL)).thenReturn(false);

    notificationService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(notificationSender, never()).send(notification);
  }
}
