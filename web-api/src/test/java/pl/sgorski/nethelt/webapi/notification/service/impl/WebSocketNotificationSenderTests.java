package pl.sgorski.nethelt.webapi.notification.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationDeliverySkippedException;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationResponse;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;

@ExtendWith(MockitoExtension.class)
public class WebSocketNotificationSenderTests {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @Mock private SimpUserRegistry userRegistry;
  @Mock private NotificationMapper notificationMapper;
  @InjectMocks private WebSocketNotificationSender webSocketNotificationSender;

  @Test
  void supports_shouldSupportWebsocketChannel() {
    var result = webSocketNotificationSender.supports(NotificationChannel.WEBSOCKET);

    assertTrue(result);
  }

  @Test
  void supports_shouldNotSupportEmailChannel() {
    var result = webSocketNotificationSender.supports(NotificationChannel.EMAIL);

    assertFalse(result);
  }

  @Test
  void send_shouldPushNotificationToUserDestination_whenUserIsConnected() {
    var notification = TestNotificationFactory.createNotification();
    var email = notification.getUser().getEmail();
    var response = new NotificationResponse(1L, "Title", "Content", Instant.now(), null);
    when(userRegistry.getUser(email)).thenReturn(mock(SimpUser.class));
    when(notificationMapper.toResponse(notification)).thenReturn(response);

    webSocketNotificationSender.send(notification);

    verify(messagingTemplate)
        .convertAndSendToUser(email, WebSocketNotificationSender.DESTINATION, response);
  }

  @Test
  void send_shouldSkipWithoutPushing_whenUserIsNotConnected() {
    var notification = TestNotificationFactory.createNotification();
    when(userRegistry.getUser(notification.getUser().getEmail())).thenReturn(null);

    assertThrows(
        NotificationDeliverySkippedException.class,
        () -> webSocketNotificationSender.send(notification));

    verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
  }
}
