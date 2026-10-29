package pl.sgorski.nethelt.webapi.notification.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationPreferencesNotFoundException;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanEventListenerTests {

  @Mock private NotificationPreferencesService notificationPreferencesService;
  @Mock private NotificationService notificationService;
  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private DiscoveryScanEventListener discoveryScanEventListener;

  @Test
  void handle_shouldSendCompletedNotificationWithDeviceCount() {
    var notification = mockNotificationCreation();

    discoveryScanEventListener.handle(event(DiscoveryScanStatus.COMPLETED, 12, null));

    verifyNotificationSent(notification, "[Nethelt] Network scan finished", "12");
  }

  @Test
  void handle_shouldSendFailedNotificationWithReason() {
    var notification = mockNotificationCreation();

    discoveryScanEventListener.handle(event(DiscoveryScanStatus.FAILED, 0, "No network interface"));

    verifyNotificationSent(notification, "[Nethelt] Network scan failed", "No network interface");
  }

  @Test
  void handle_shouldSendTimedOutNotification() {
    var notification = mockNotificationCreation();

    discoveryScanEventListener.handle(event(DiscoveryScanStatus.TIMED_OUT, 0, null));

    verifyNotificationSent(notification, "[Nethelt] Network scan timed out", "in time");
  }

  @ParameterizedTest
  @EnumSource(
      value = DiscoveryScanStatus.class,
      names = {"PENDING", "RUNNING"})
  void handle_shouldNotSend_whenScanIsInProgress(DiscoveryScanStatus status) {
    discoveryScanEventListener.handle(event(status, 0, null));

    verifyNoInteractions(
        notificationPreferencesService, notificationService, notificationDeliveryService);
  }

  @Test
  void handle_shouldNotSend_whenErrorOccurs() {
    when(notificationPreferencesService.getPreferences(1L))
        .thenThrow(NotificationPreferencesNotFoundException.class);

    discoveryScanEventListener.handle(event(DiscoveryScanStatus.COMPLETED, 3, null));

    verify(notificationDeliveryService, never()).send(any(), any());
    verify(notificationService, never()).create(any());
  }

  private static DiscoveryScanStatusChangedEvent event(
      DiscoveryScanStatus status, int resultCount, @Nullable String failureReason) {
    return new DiscoveryScanStatusChangedEvent(1L, 2L, "Home", status, resultCount, failureReason);
  }

  private Notification mockNotificationCreation() {
    var user = TestUserFactory.createLocalUser();
    var notification = new Notification(user, "test", "test");
    when(notificationPreferencesService.getPreferences(1L))
        .thenReturn(user.getNotificationPreferences());
    when(notificationService.create(any())).thenReturn(notification);
    return notification;
  }

  private void verifyNotificationSent(Notification notification, String title, String detail) {
    verify(notificationService)
        .create(
            argThat(
                command ->
                    command.userId().equals(1L)
                        && command.title().equals(title)
                        && command.content().contains("Home")
                        && command.content().contains(detail)));
    verify(notificationDeliveryService)
        .send(
            notification, notification.getUser().getNotificationPreferences().getEnabledChannels());
  }
}
