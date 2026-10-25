package pl.sgorski.nethelt.webapi.notification.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationPreferencesNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOfflineEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOnlineEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentTokenRenewedEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class AgentEventListenerTests {

  @Mock private NotificationPreferencesService notificationPreferencesService;
  @Mock private NotificationService notificationService;
  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private AgentEventListener agentEventListener;

  @Test
  void handle_shouldSendDisabledNotification() {
    var notification = mockNotificationCreation();

    agentEventListener.handle(
        new AgentStatusChangedEvent(1L, "Home", "Edge", AgentStatus.DISABLED));

    verifyNotificationSent(notification, "[Nethelt] Agent disabled");
  }

  @Test
  void handle_shouldSendActivatedNotification() {
    var notification = mockNotificationCreation();

    agentEventListener.handle(new AgentStatusChangedEvent(1L, "Home", "Edge", AgentStatus.ACTIVE));

    verifyNotificationSent(notification, "[Nethelt] Agent activated");
  }

  @Test
  void handle_shouldSendTokenRenewedNotification() {
    var notification = mockNotificationCreation();

    agentEventListener.handle(new AgentTokenRenewedEvent(1L, "Home", "Edge"));

    verifyNotificationSent(notification, "[Nethelt] Agent token regenerated");
  }

  @Test
  void handle_shouldSendOfflineNotification() {
    var notification = mockNotificationCreation();

    agentEventListener.handle(
        new AgentOfflineEvent(
            1L, "Home", "Edge", Instant.parse("2026-01-01T12:00:00Z"), Duration.ofMinutes(5)));

    verify(notificationService)
        .create(
            argThat(
                command ->
                    command.title().equals("[Nethelt] Agent offline")
                        && command.content().contains("5 min")
                        && command.content().contains("2026-01-01T12:00:00Z")));
    verify(notificationDeliveryService).send(eq(notification), any());
  }

  @Test
  void handle_shouldSendOnlineNotification() {
    var notification = mockNotificationCreation();

    agentEventListener.handle(new AgentOnlineEvent(1L, "Home", "Edge"));

    verifyNotificationSent(notification, "[Nethelt] Agent back online");
  }

  @Test
  void handle_shouldNotSend_whenErrorOccurs() {
    when(notificationPreferencesService.getPreferences(1L))
        .thenThrow(NotificationPreferencesNotFoundException.class);

    agentEventListener.handle(new AgentOnlineEvent(1L, "Home", "Edge"));

    verify(notificationDeliveryService, never()).send(any(), any());
    verify(notificationService, never()).create(any());
  }

  private Notification mockNotificationCreation() {
    var user = TestUserFactory.createLocalUser();
    var notification = new Notification(user, "test", "test");
    when(notificationPreferencesService.getPreferences(1L))
        .thenReturn(user.getNotificationPreferences());
    when(notificationService.create(any())).thenReturn(notification);
    return notification;
  }

  private void verifyNotificationSent(Notification notification, String title) {
    verify(notificationService)
        .create(
            argThat(
                command ->
                    command.userId().equals(1L)
                        && command.title().equals(title)
                        && command.content().contains("Edge")
                        && command.content().contains("Home")));
    verify(notificationDeliveryService)
        .send(
            notification, notification.getUser().getNotificationPreferences().getEnabledChannels());
  }
}
